package nickle.scheduler.server.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import lombok.extern.slf4j.Slf4j;
import nickle.scheduler.common.cron.CronExpression;
import nickle.scheduler.common.event.RegisterEvent;
import nickle.scheduler.server.entity.NickleSchedulerExecutor;
import nickle.scheduler.server.entity.NickleSchedulerExecutorJob;
import nickle.scheduler.server.entity.NickleSchedulerJob;
import nickle.scheduler.server.entity.NickleSchedulerTrigger;
import nickle.scheduler.server.mapper.NickleSchedulerExecutorJobMapper;
import nickle.scheduler.server.mapper.NickleSchedulerExecutorMapper;
import nickle.scheduler.server.mapper.NickleSchedulerJobMapper;
import nickle.scheduler.server.mapper.NickleSchedulerTriggerMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Date;
import java.util.List;

import static nickle.scheduler.common.Constant.REGISTER_OK;

/**
 * @author nickle
 * @description:
 * @date 2019/5/8 14:02
 */
@Slf4j
public class RegisterActor extends AbstractActor {
    private static final String LOCK_NAME = "register_lock";
    private SqlSessionFactory sqlSessionFactory;

    public RegisterActor(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public static Props props(SqlSessionFactory sqlSessionFactory) {
        return Props.create(RegisterActor.class, () -> new RegisterActor(sqlSessionFactory));
    }

    @Override
    public void preStart() throws Exception {
        log.info("注册器启动");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(RegisterEvent.class, this::registerExecutorAndJob).build();
    }

    public void registerExecutorAndJob(RegisterEvent registerEvent) {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        log.info("注册开始:{}", registerEvent);
        try {
            /**
             * 防止重复插入先查询任务主机和触发器是否存在
             * @TODO
             */
            insertExecutorAndJob(sqlSession, registerEvent);
            insertTrigger(sqlSession, registerEvent);
            sqlSession.commit();
            //通知注册成功
            getSender().tell(REGISTER_OK, getSelf());
        } catch (Exception e) {
            sqlSession.rollback();
            log.error("注册发生错误:{}", e.getMessage());
        } finally {
            sqlSession.close();
        }
        log.info("注册结束");
    }


    private void insertExecutorAndJob(SqlSession sqlSession, RegisterEvent registerEvent) {
        NickleSchedulerExecutorMapper executorMapper = sqlSession.getMapper(NickleSchedulerExecutorMapper.class);
        NickleSchedulerJobMapper jobMapper = sqlSession.getMapper(NickleSchedulerJobMapper.class);
        NickleSchedulerExecutorJobMapper executorJobMapper = sqlSession.getMapper(NickleSchedulerExecutorJobMapper.class);
        //插入执行器
        NickleSchedulerExecutor nickleSchedulerExecutor = new NickleSchedulerExecutor();
        nickleSchedulerExecutor.setExecutorIp(registerEvent.getIp());
        nickleSchedulerExecutor.setExecutorPort(registerEvent.getPort());
        nickleSchedulerExecutor.setUpdateTime(System.currentTimeMillis());
        log.info("插入执行器:{}", nickleSchedulerExecutor);
        executorMapper.insert(nickleSchedulerExecutor);
        //插入job
        List<RegisterEvent.JobData> jobDataList = registerEvent.getJobDataList();
        for (RegisterEvent.JobData job : jobDataList) {
            NickleSchedulerJob nickleSchedulerJob = new NickleSchedulerJob();
            nickleSchedulerJob.setJobAuthor(job.getJobAuthor());
            nickleSchedulerJob.setJobClassName(job.getJobClassName());
            nickleSchedulerJob.setJobDescription(job.getJobDescription());
            nickleSchedulerJob.setJobTriggerName(job.getJobTriggerName());
            nickleSchedulerJob.setJobName(job.getJobName());
            log.info("插入job:{}", nickleSchedulerJob);
            jobMapper.insert(nickleSchedulerJob);
            //插入关联表
            NickleSchedulerExecutorJob executorJob = new NickleSchedulerExecutorJob();
            executorJob.setExecutorId(nickleSchedulerExecutor.getExecutorId());
            executorJob.setJobId(nickleSchedulerJob.getId());
            log.info("插入executor和job的关联表:{}", nickleSchedulerJob);
            executorJobMapper.insert(executorJob);
        }
    }

    private void insertTrigger(SqlSession sqlSession, RegisterEvent registerEvent) throws Exception {
        NickleSchedulerTriggerMapper triggerMapper = sqlSession.getMapper(NickleSchedulerTriggerMapper.class);
        for (RegisterEvent.TriggerData triggerData : registerEvent.getTriggerDataList()) {
            NickleSchedulerTrigger trigger = new NickleSchedulerTrigger();
            trigger.setTriggerCron(triggerData.getTriggerCron());
            trigger.setTriggerName(triggerData.getTriggerName());
            CronExpression cronExpression = new CronExpression(trigger.getTriggerCron());
            trigger.setTriggerNextTime(cronExpression.getTimeAfter(new Date()).getTime());
            log.info("插入trigger:{}", trigger);
            triggerMapper.insert(trigger);
        }
    }

}
