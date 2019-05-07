package nickle.scheduler.server.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import nickle.scheduler.common.cron.CronExpression;
import nickle.scheduler.common.event.ExecuteJobEvent;
import nickle.scheduler.server.entity.*;
import nickle.scheduler.server.mapper.*;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static nickle.scheduler.common.Constant.*;


/**
 * @author nickle
 * @description:
 * @date 2019/5/7 14:59
 */
@Slf4j
public class SchedulerActor extends AbstractActor {
    private final String LOCK_NAME = "schedule_lock";
    private final int SCHEDULE_TIME = 10 * 1000;

    public static Props props(SqlSessionFactory sqlSessionFactory) {
        return Props.create(SchedulerActor.class, () -> new SchedulerActor(sqlSessionFactory));
    }

    public SchedulerActor(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    private SqlSessionFactory sqlSessionFactory;

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchAny(msg -> cycleSchedule()).build();
    }

    private void cycleSchedule() {
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
            //添加任务
            addRunJob(sqlSession, nickleSchedulerTriggers, schedulerTriggerMapper);
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            log.error("调度发生错误:{}", e.getMessage());
        } finally {
            sqlSession.close();
        }
        nextSchedule();
    }

    /**
     * 更新trigger的下一次执行时间并通知执行器执行任务
     *
     * @param nickleSchedulerTriggers
     */
    private void addRunJob(SqlSession sqlSession, List<NickleSchedulerTrigger> nickleSchedulerTriggers, NickleSchedulerTriggerMapper schedulerTriggerMapper) throws ParseException {
        NickleSchedulerRunJobMapper runJobMapper = sqlSession.getMapper(NickleSchedulerRunJobMapper.class);
        NickleSchedulerJobMapper jobMapper = sqlSession.getMapper(NickleSchedulerJobMapper.class);
        NickleSchedulerExecutorMapper executorMapper = sqlSession.getMapper(NickleSchedulerExecutorMapper.class);
        NickleSchedulerExecutorJobMapper executorJobMapper = sqlSession.getMapper(NickleSchedulerExecutorJobMapper.class);
        for (NickleSchedulerTrigger trigger : nickleSchedulerTriggers) {
            // 通知执行器执行job
            QueryWrapper<NickleSchedulerJob> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(NickleSchedulerJob::getJobTriggerName, trigger.getTriggerName());
            List<NickleSchedulerJob> nickleSchedulerRunJobs = jobMapper.selectList(queryWrapper);
            log.info("需要调度的job：{}", nickleSchedulerRunJobs);
            for (NickleSchedulerJob job : nickleSchedulerRunJobs) {
                //获取到job对应执行器，选取发送
                QueryWrapper<NickleSchedulerExecutorJob> executorJobQueryWrapper = new QueryWrapper<>();
                executorJobQueryWrapper.lambda().eq(NickleSchedulerExecutorJob::getJobId, job.getId());
                List<NickleSchedulerExecutorJob> nickleSchedulerExecutorJobs = executorJobMapper.selectList(executorJobQueryWrapper);
                //默认选择第一个
                if (nickleSchedulerExecutorJobs.isEmpty()) {
                    //此时该job的执行器为空，待处理
                } else {
                    //获取执行器ip和端口
                    NickleSchedulerExecutorJob nickleSchedulerExecutorJob = nickleSchedulerExecutorJobs.get(0);
                    QueryWrapper<NickleSchedulerExecutor> schedulerExecutorQueryWrapper = new QueryWrapper<>();
                    schedulerExecutorQueryWrapper.lambda().eq(NickleSchedulerExecutor::getExecutorId, nickleSchedulerExecutorJob.getExecutorId());
                    NickleSchedulerExecutor nickleSchedulerExecutor = executorMapper.selectOne(schedulerExecutorQueryWrapper);
                    //拼接获取远程actor发送信息
                    ActorSelection actorSelection = getContext().actorSelection(String.format(AKKA_REMOTE_MODEL
                            , nickleSchedulerExecutor.getExecutorIp()
                            , nickleSchedulerExecutor.getExecutorPort()
                            , EXECUTOR_SYSTEM_NAME
                            , EXECUTOR_DISPATCHER_NAME));
                    ExecuteJobEvent executorStartEvent = new ExecuteJobEvent();
                    executorStartEvent.setClassName(job.getJobClassName());
                    log.info("开始调度job：{}", job);
                    actorSelection.tell(executorStartEvent, getSelf());
                    //插入运行队列
                    NickleSchedulerRunJob nickleSchedulerRunJob = new NickleSchedulerRunJob();
                    nickleSchedulerRunJob.setExecutorId(nickleSchedulerExecutorJob.getExecutorId());
                    nickleSchedulerRunJob.setJobName(job.getJobName());
                    nickleSchedulerRunJob.setScheduleTime(System.currentTimeMillis());
                    nickleSchedulerRunJob.setTriggerName(job.getJobTriggerName());
                    nickleSchedulerRunJob.setUpdateTime(System.currentTimeMillis());
                    runJobMapper.insert(nickleSchedulerRunJob);
                }

            }
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
        getSender().tell("", getSelf());
    }
}
