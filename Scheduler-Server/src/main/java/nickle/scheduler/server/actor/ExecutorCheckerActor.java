package nickle.scheduler.server.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import nickle.scheduler.server.entity.NickleSchedulerExecutor;
import nickle.scheduler.server.entity.NickleSchedulerFailJob;
import nickle.scheduler.server.entity.NickleSchedulerJob;
import nickle.scheduler.server.entity.NickleSchedulerRunJob;
import nickle.scheduler.server.mapper.*;
import nickle.scheduler.server.util.Delegate;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author nickle
 * @description: 执行检测任务，发现超时心跳主机，清除超时心跳主机并将该主机上的任务重启
 * @date 2019/5/7 19:10
 */
@Slf4j
public class ExecutorCheckerActor extends AbstractActor {
    private static final String LOCK_NAME = "executor_lock";
    public static final String CHECK = "CHECK";
    /**
     * 默认每20秒检测一次
     */
    private static final long CHECK_TIME = 20 * 1000L;

    @Override
    public void preStart() throws Exception {
        log.info("检测器启动");
        cycleCheck(CHECK);
    }

//    /**
//     * 10s间隔内没有收到任务心跳标明任务失败
//     */
//    private static final long RUN_JOB_HEART_BEAT_INTERVAL = 10 * 1000L;
    /**
     * 10s间隔内没有收到执行器心跳标明执行器down掉
     */
    private static final long EXECUTOR_HEART_BEAT_INTERVAL = 10 * 1000L;
    private SqlSessionFactory sqlSessionFactory;

    public static Props props(SqlSessionFactory sqlSessionFactory) {
        return Props.create(ExecutorCheckerActor.class, () -> new ExecutorCheckerActor(sqlSessionFactory));
    }


    public ExecutorCheckerActor(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchEquals(CHECK, this::cycleCheck).build();
    }

    private void cycleCheck(String msg) {
        checkFailExecutor();
    }

    /**
     * 检测已经失去心跳的执行器并执行删除，并且重调度该执行器的任务
     */
    private void checkFailExecutor() {
        log.info("开始检测无心跳主机");
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            NickleSchedulerLockMapper lockMapper = sqlSession.getMapper(NickleSchedulerLockMapper.class);
            //上锁
            lockMapper.lock(LOCK_NAME);
            //删除主机
            NickleSchedulerExecutorMapper executorMapper = sqlSession.getMapper(NickleSchedulerExecutorMapper.class);
            QueryWrapper<NickleSchedulerExecutor> executorQueryWrapper = new QueryWrapper<>();
            executorQueryWrapper.lambda().le(NickleSchedulerExecutor::getUpdateTime, System.currentTimeMillis() - EXECUTOR_HEART_BEAT_INTERVAL);
            List<NickleSchedulerExecutor> schedulerExecutors = executorMapper.selectList(executorQueryWrapper);
            if (!ObjectUtils.isEmpty(schedulerExecutors)) {
                log.info("删除过期主机:{}", schedulerExecutors);
                Delegate.deleteExecutor(sqlSession, schedulerExecutors.toArray(new NickleSchedulerExecutor[]{}));
                //这里需要一个新的sqlsession保证不死锁
                sqlSession.commit();
                sqlSession.close();
                sqlSession = sqlSessionFactory.openSession(false);
                //删除主机关联的job并重调度该执行器下的job
                for (NickleSchedulerExecutor schedulerExecutor : schedulerExecutors) {
                    //重调度
                    reSchedulerDeathExecutorJob(schedulerExecutor.getExecutorId(), sqlSession);
                }
            } else {
                log.info("结束检测无心跳主机,无过期主机");
            }
            sqlSession.commit();
        } catch (Exception e) {
            log.error("检测发生错误:{}", e.getMessage());
        } finally {
            sqlSession.close();
        }
        log.info("结束检测无心跳主机");
        nextCheck();
    }

    /**
     * 重调度失败的任务
     */
    private void reSchedulerDeathExecutorJob(Integer executorId, SqlSession sqlSession) {
        NickleSchedulerRunJobMapper runJobMapper = sqlSession.getMapper(NickleSchedulerRunJobMapper.class);
        NickleSchedulerJobMapper jobMapper = sqlSession.getMapper(NickleSchedulerJobMapper.class);
        //获取需要重调度任务
        QueryWrapper<NickleSchedulerRunJob> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(NickleSchedulerRunJob::getExecutorId, executorId);
        List<NickleSchedulerRunJob> nickleSchedulerRunJobs = runJobMapper.selectList(queryWrapper);
        log.info("需要重调度的任务:{}", nickleSchedulerRunJobs);
        if (!CollectionUtils.isEmpty(nickleSchedulerRunJobs)) {
            //立即重调度
            //删除过期，将过期任务放入失败任务表
            List<Integer> collect = nickleSchedulerRunJobs.stream().map(NickleSchedulerRunJob::getId).collect(Collectors.toList());
            runJobMapper.deleteBatchIds(collect);
            writeFailJob(nickleSchedulerRunJobs, sqlSession);
            //重调度
            List<NickleSchedulerJob> nickleSchedulerJobs = jobMapper.selectBatchIds(nickleSchedulerRunJobs.stream().map(NickleSchedulerRunJob::getJobId).collect(Collectors.toList()));
            Delegate.scheduleJob(nickleSchedulerJobs, sqlSessionFactory, getContext());
        }
    }

    /**
     * 写入失败表
     *
     * @param nickleSchedulerRunJobs
     * @param sqlSession
     */
    private void writeFailJob(List<NickleSchedulerRunJob> nickleSchedulerRunJobs, SqlSession sqlSession) {
        NickleSchedulerFailJobMapper failJobMapper = sqlSession.getMapper(NickleSchedulerFailJobMapper.class);
        for (NickleSchedulerRunJob runJob : nickleSchedulerRunJobs) {
            NickleSchedulerFailJob nickleSchedulerFailJob = new NickleSchedulerFailJob();
            nickleSchedulerFailJob.setTriggerName(runJob.getTriggerName());
            nickleSchedulerFailJob.setJobName(runJob.getJobName());
            nickleSchedulerFailJob.setFailReason((byte) 2);
            nickleSchedulerFailJob.setJobId(runJob.getJobId());
            nickleSchedulerFailJob.setExecutorId(runJob.getExecutorId());
            nickleSchedulerFailJob.setFailedTime(System.currentTimeMillis());
            failJobMapper.insert(nickleSchedulerFailJob);
        }
    }

    private void nextCheck() {
        try {
            Thread.sleep(CHECK_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getSelf().tell(CHECK, getSelf());
    }
}
