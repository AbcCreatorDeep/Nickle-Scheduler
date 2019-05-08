package nickle.scheduler.server.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import nickle.scheduler.common.cron.CronExpression;
import nickle.scheduler.server.entity.NickleSchedulerJob;
import nickle.scheduler.server.entity.NickleSchedulerTrigger;
import nickle.scheduler.server.mapper.NickleSchedulerJobMapper;
import nickle.scheduler.server.mapper.NickleSchedulerLockMapper;
import nickle.scheduler.server.mapper.NickleSchedulerTriggerMapper;
import nickle.scheduler.server.util.Utils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.List;


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
    private static final long SCHEDULE_TIME = 20 * 1000L;
    private SqlSessionFactory sqlSessionFactory;

    @Override
    public void preStart() throws Exception {
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
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            NickleSchedulerTriggerMapper schedulerTriggerMapper = sqlSession.getMapper(NickleSchedulerTriggerMapper.class);
            NickleSchedulerLockMapper schedulerLockMapper = sqlSession.getMapper(NickleSchedulerLockMapper.class);
            //获取锁
            schedulerLockMapper.lock(LOCK_NAME);
            //查询需要执行的任务
            QueryWrapper<NickleSchedulerTrigger> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().le(NickleSchedulerTrigger::getTriggerNextTime, System.currentTimeMillis());
            List<NickleSchedulerTrigger> nickleSchedulerTriggers = schedulerTriggerMapper.selectList(queryWrapper);
            log.info("需要调度的触发器：{}", nickleSchedulerTriggers);
            if (ObjectUtils.isEmpty(nickleSchedulerTriggers)) {
                log.info("结束扫描待调度任务,无调度触发器");
                nextSchedule();
                return;
            }
            //添加任务
            addRunJob(sqlSession, nickleSchedulerTriggers, schedulerTriggerMapper);
            sqlSession.commit();
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
     * 更新trigger的下一次执行时间并通知执行器执行任务
     *
     * @param nickleSchedulerTriggers
     */
    private void addRunJob(SqlSession sqlSession, List<NickleSchedulerTrigger> nickleSchedulerTriggers, NickleSchedulerTriggerMapper schedulerTriggerMapper) throws ParseException {
        NickleSchedulerJobMapper jobMapper = sqlSession.getMapper(NickleSchedulerJobMapper.class);
        for (NickleSchedulerTrigger trigger : nickleSchedulerTriggers) {
            // 通知执行器执行job
            QueryWrapper<NickleSchedulerJob> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(NickleSchedulerJob::getJobTriggerName, trigger.getTriggerName());
            List<NickleSchedulerJob> nickleSchedulerRunJobs = jobMapper.selectList(queryWrapper);
            //调度任务
            Utils.scheduleJob(nickleSchedulerRunJobs, sqlSession, getContext(), getSelf());
            //修改触发器下次执行时间
            String triggerCron = trigger.getTriggerCron();
            CronExpression cronExpression = new CronExpression(triggerCron);
            long time = cronExpression.getTimeAfter(new Date()).getTime();
            trigger.setTriggerNextTime(time);
            schedulerTriggerMapper.updateById(trigger);
        }

    }


    private void nextSchedule() {
        try {
            Thread.sleep(SCHEDULE_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getSelf().tell(SCHEDULE, getSelf());
    }
}
