package nickle.scheduler.server.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

import static nickle.scheduler.common.Constant.*;

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
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        log.info("注册开始:{}", registerEvent);
        try {
            insertExecutorAndJob(sqlSession, registerEvent);
            insertTrigger(sqlSession, registerEvent);
            sqlSession.commit();
            //通知注册成功
            getSender().tell(REGISTER_OK, getSelf());
        } catch (Exception e) {
            sqlSession.rollback();
            log.error("注册发生错误:{}", e.getMessage());
            //通知注册失败
            getSender().tell(REGISTER_FAIL, getSelf());
        } finally {
            sqlSession.close();
        }
        log.info("注册结束");
    }

    /**
     * 检测执行器是否存在
     *
     * @param executorMapper
     * @param nickleSchedulerExecutor
     * @return
     */
    private boolean checkExecutor(NickleSchedulerExecutorMapper executorMapper, NickleSchedulerExecutor nickleSchedulerExecutor) {
        QueryWrapper<NickleSchedulerExecutor> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(NickleSchedulerExecutor::getExecutorIp, nickleSchedulerExecutor.getExecutorIp())
                .eq(NickleSchedulerExecutor::getExecutorPort, nickleSchedulerExecutor.getExecutorPort());
        return executorMapper.selectOne(queryWrapper) == null;
    }

    /**
     * 检测触发器是否存在
     *
     * @param triggerMapper
     * @param nickleSchedulerTrigger
     * @return
     */
    private boolean checkTrigger(NickleSchedulerTriggerMapper triggerMapper, NickleSchedulerTrigger nickleSchedulerTrigger) {
        QueryWrapper<NickleSchedulerTrigger> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(NickleSchedulerTrigger::getTriggerName, nickleSchedulerTrigger.getTriggerName());
        return triggerMapper.selectOne(queryWrapper) == null;
    }

    /**
     * 检测任务是否存在
     *
     * @param executorJobMapper
     * @param nickleSchedulerJob
     * @return
     */
    private boolean checkJob(NickleSchedulerJobMapper executorJobMapper, NickleSchedulerJob nickleSchedulerJob) {
        QueryWrapper<NickleSchedulerJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(NickleSchedulerJob::getJobName, nickleSchedulerJob.getJobName());
        return executorJobMapper.selectOne(queryWrapper) == null;
    }

    private void insertExecutorAndJob(SqlSession sqlSession, RegisterEvent registerEvent) {
        NickleSchedulerExecutorMapper executorMapper = sqlSession.getMapper(NickleSchedulerExecutorMapper.class);
        NickleSchedulerJobMapper jobMapper = sqlSession.getMapper(NickleSchedulerJobMapper.class);
        NickleSchedulerExecutorJobMapper executorJobMapper = sqlSession.getMapper(NickleSchedulerExecutorJobMapper.class);
        //插入执行器
        String ipPortStr = String.format(SOCKET_ADDRESS_MODEL, registerEvent.getIp(), registerEvent.getPort());
        NickleSchedulerExecutor nickleSchedulerExecutor = new NickleSchedulerExecutor();
        nickleSchedulerExecutor.setExecutorIp(registerEvent.getIp());
        nickleSchedulerExecutor.setExecutorPort(registerEvent.getPort());
        nickleSchedulerExecutor.setUpdateTime(System.currentTimeMillis());
        nickleSchedulerExecutor.setExecutorName(ipPortStr);
        if (checkExecutor(executorMapper, nickleSchedulerExecutor)) {
            log.info("插入执行器:{}", nickleSchedulerExecutor);
            executorMapper.insert(nickleSchedulerExecutor);
        } else {
            log.info("执行器存在，忽略");
        }
        //插入job
        List<RegisterEvent.JobData> jobDataList = registerEvent.getJobDataList();
        for (RegisterEvent.JobData job : jobDataList) {
            NickleSchedulerJob nickleSchedulerJob = new NickleSchedulerJob();
            nickleSchedulerJob.setJobAuthor(job.getJobAuthor());
            nickleSchedulerJob.setJobClassName(job.getJobClassName());
            nickleSchedulerJob.setJobDescription(job.getJobDescription());
            nickleSchedulerJob.setJobTriggerName(job.getJobTriggerName());
            nickleSchedulerJob.setJobName(job.getJobName());
            if (checkJob(jobMapper, nickleSchedulerJob)) {
                log.info("插入job:{}", nickleSchedulerJob);
                jobMapper.insert(nickleSchedulerJob);
            } else {
                log.info("job存在，忽略");
            }
            //插入关联表
            NickleSchedulerExecutorJob executorJob = new NickleSchedulerExecutorJob();
            executorJob.setExecutorName(ipPortStr);
            executorJob.setJobName(nickleSchedulerJob.getJobName());
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
            if (checkTrigger(triggerMapper, trigger)) {
                log.info("插入trigger:{}", trigger);
                triggerMapper.insert(trigger);
            } else {
                log.info("触发器存在，忽略");
            }

        }
    }

}
