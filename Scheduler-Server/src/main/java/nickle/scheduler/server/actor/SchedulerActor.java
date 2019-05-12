package nickle.scheduler.server.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import nickle.scheduler.common.cron.CronExpression;
import nickle.scheduler.server.entity.NickleSchedulerJob;
import nickle.scheduler.server.entity.NickleSchedulerTrigger;
import nickle.scheduler.server.mapper.NickleSchedulerJobMapper;
import nickle.scheduler.server.mapper.NickleSchedulerLockMapper;
import nickle.scheduler.server.mapper.NickleSchedulerTriggerMapper;
import nickle.scheduler.server.util.Delegate;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static nickle.scheduler.server.constant.Constant.ACQUIRED;
import static nickle.scheduler.server.constant.Constant.STAND_BY;


/**
 * @author nickle
 * @description:
 * @date 2019/5/7 14:59
 */
@Slf4j
public class SchedulerActor extends AbstractActor {
    private static final String LOCK_NAME = "trigger_lock";
    public static final String SCHEDULE = "SCHEDULE";
    /**
     * 默认每十秒调度一次
     */
    private static final long SCHEDULE_TIME = 5 * 1000L;
    /**
     * 容错时间
     */
    private static final long MISTAKE_TIME = 10 * 1000L;
    /**
     * 触发器精确时间
     */
    private static final long TRIGGER_TIME = 1000L;
    /**
     * 一次调度的触发器个数，为防止获取太多调度任务设置上限
     */
    private static final long SCHEDULE_TRIGGER_NUM = 10;
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void preStart() {
        log.info("调度器启动");
        cycleSchedule(SCHEDULE);
    }

    public static Props props(SqlSessionFactory sqlSessionFactory) {
        return Props.create(SchedulerActor.class, () -> new SchedulerActor(sqlSessionFactory));
    }

    public SchedulerActor(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchEquals(SCHEDULE, this::cycleSchedule).build();
    }

    private void cycleSchedule(String msg) {
        log.info("开始扫描待调度任务");
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            NickleSchedulerTriggerMapper schedulerTriggerMapper = sqlSession.getMapper(NickleSchedulerTriggerMapper.class);
            NickleSchedulerLockMapper schedulerLockMapper = sqlSession.getMapper(NickleSchedulerLockMapper.class);
            //获取锁防止多个schduler同时获取到相同的任务
            schedulerLockMapper.lock(LOCK_NAME);
            //查询需要执行的任务
            QueryWrapper<NickleSchedulerTrigger> queryWrapper = new QueryWrapper<>();
            /**
             * 一次性获取到下一次待调度的trigger，按需要调度时间降序，获取条件（满足一条即可）：
             * 1、触发器状态为STAND_BY且时间在当前时间+调度时间内
             * 2、触发器状态为ACQUIRED但更新时间小于一次性获取的时间+容错时间(为了防止调度器修改完状态后down机)
             */
            log.info("autocommit:{}", sqlSession.getConnection().getAutoCommit());
            queryWrapper.lambda()
                    .and(qw -> qw.le(NickleSchedulerTrigger::getTriggerNextTime, System.currentTimeMillis())
                            .eq(NickleSchedulerTrigger::getTriggerStatus, STAND_BY))
                    .or(qw1 -> qw1.and(qw2 -> qw2.eq(NickleSchedulerTrigger::getTriggerStatus, ACQUIRED)
                            .le(NickleSchedulerTrigger::getTriggerUpdateTime, System.currentTimeMillis() - (SCHEDULE_TIME + MISTAKE_TIME))))
                    .orderByDesc(NickleSchedulerTrigger::getTriggerNextTime);
            //分页取
            Page<NickleSchedulerTrigger> triggerPage = new Page<>(1, SCHEDULE_TRIGGER_NUM);
            List<NickleSchedulerTrigger> nickleSchedulerTriggers = schedulerTriggerMapper.selectPage(triggerPage, queryWrapper).getRecords();
            log.info("需要调度的触发器：{}", nickleSchedulerTriggers);
            if (ObjectUtils.isEmpty(nickleSchedulerTriggers)) {
                log.info("结束扫描待调度任务,无调度触发器");
                nextSchedule();
                return;
            }
            //修改trigger状态后释放锁
            modifyTriggerStatus(nickleSchedulerTriggers, sqlSession);
            //添加任务
            addRunJob(nickleSchedulerTriggers);
        } catch (Exception e) {
            sqlSession.rollback();
            e.printStackTrace();
            log.error("调度发生错误:{}", e.getMessage());
        } finally {
            sqlSession.close();
        }
        log.info("结束扫描待调度任务");
        nextSchedule();
    }

    /**
     * 更改执行器状态后立即提交，减少锁持有时间
     *
     * @param nickleSchedulerTriggers
     * @param sqlSession
     */
    private void modifyTriggerStatus(List<NickleSchedulerTrigger> nickleSchedulerTriggers, SqlSession sqlSession) {
        NickleSchedulerTriggerMapper schedulerTriggerMapper = sqlSession.getMapper(NickleSchedulerTriggerMapper.class);
        for (NickleSchedulerTrigger trigger : nickleSchedulerTriggers) {
            trigger.setTriggerStatus(ACQUIRED);
            trigger.setTriggerUpdateTime(System.currentTimeMillis());
            schedulerTriggerMapper.updateById(trigger);
        }
        sqlSession.commit();
        sqlSession.close();
    }

    /**
     * 更新trigger的下一次执行时间并通知执行器执行任务
     *
     * @param nickleSchedulerTriggers
     */
    private void addRunJob(List<NickleSchedulerTrigger> nickleSchedulerTriggers) throws ParseException, InterruptedException {
        //由于降序排列所以以第一个为准,精准度控制在1s内
        NickleSchedulerTrigger nickleSchedulerTrigger = nickleSchedulerTriggers.get(0);
        long waitTime = nickleSchedulerTrigger.getTriggerNextTime() - System.currentTimeMillis();
        if (waitTime > TRIGGER_TIME) {
            //睡眠直到任务可执行
            log.info("一次性获取任务数：{},等待时间:{}", nickleSchedulerTriggers.size(), waitTime);
            Thread.sleep(waitTime);
        }
        SqlSession sqlSession = sqlSessionFactory.openSession();
        ArrayList<List<NickleSchedulerJob>> jobListList = Lists.newArrayList();
        try {
            NickleSchedulerTriggerMapper schedulerTriggerMapper = sqlSession.getMapper(NickleSchedulerTriggerMapper.class);
            NickleSchedulerJobMapper jobMapper = sqlSession.getMapper(NickleSchedulerJobMapper.class);
            for (NickleSchedulerTrigger trigger : nickleSchedulerTriggers) {
                // 获取触发器对应的job
                QueryWrapper<NickleSchedulerJob> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(NickleSchedulerJob::getJobTriggerName, trigger.getTriggerName());
                List<NickleSchedulerJob> nickleSchedulerRunJobs = jobMapper.selectList(queryWrapper);
                jobListList.add(nickleSchedulerRunJobs);
                //修改触发器下次执行时间
                String triggerCron = trigger.getTriggerCron();
                CronExpression cronExpression = new CronExpression(triggerCron);
                long time = cronExpression.getTimeAfter(new Date()).getTime();
                Date date = new Date();
                date.setTime(time);
                log.info("下一次调度时间为:{}", date);
                trigger.setTriggerNextTime(time);
                //恢复触发器状态
                trigger.setTriggerStatus(STAND_BY);
                schedulerTriggerMapper.updateById(trigger);
            }
            sqlSession.commit();
        } finally {
            sqlSession.close();
        }
        //调度任务开始
        jobListList.parallelStream().forEach(jobList -> Delegate.scheduleJob(jobList, sqlSessionFactory, getContext()));
    }

    /**
     * 下一次调度
     */
    private void nextSchedule() {
        try {
            //由于多个调度器，采用随机睡眠增加调度效率和准确度
            Thread.sleep(SCHEDULE_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getSelf().tell(SCHEDULE, getSelf());
    }
}
