package nickle.scheduler.server.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import lombok.extern.slf4j.Slf4j;
import nickle.scheduler.common.event.ExecuteResultEvent;
import nickle.scheduler.server.entity.NickleSchedulerRunJob;
import nickle.scheduler.server.entity.NickleSchedulerSuccessJob;
import nickle.scheduler.server.mapper.NickleSchedulerRunJobMapper;
import nickle.scheduler.server.mapper.NickleSchedulerSuccessJobMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import static nickle.scheduler.common.Constant.EXECUTOR_JOB_OK;

/**
 * 任务完成处理器
 *
 * @author nickle
 * @description:
 * @date 2019/5/12 14:02
 */
@Slf4j
public class CompleteJobActor extends AbstractActor {
    private SqlSessionFactory sqlSessionFactory;

    public CompleteJobActor(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    public static Props props(SqlSessionFactory sqlSessionFactory) {
        return Props.create(CompleteJobActor.class, () -> new CompleteJobActor(sqlSessionFactory));
    }

    @Override
    public void preStart() {
        log.info("任务完成处理器启动");
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ExecuteResultEvent.class, this::execCompleteJob).build();
    }

    private void execCompleteJob(ExecuteResultEvent executeResultEvent) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            NickleSchedulerRunJobMapper runJobMapper = sqlSession.getMapper(NickleSchedulerRunJobMapper.class);
            NickleSchedulerSuccessJobMapper successJobMapper = sqlSession.getMapper(NickleSchedulerSuccessJobMapper.class);
            NickleSchedulerRunJob nickleSchedulerRunJob = runJobMapper.selectById(executeResultEvent.getJobId());
            if (nickleSchedulerRunJob == null) {
                log.error("任务超时被删除，但是却传来了任务完成信息");
                this.getSender().tell(EXECUTOR_JOB_OK, getSelf());
                return;
            }
            //删除运行任务表，将任务插入成功列表,并返回成功给客户端
            runJobMapper.deleteById(executeResultEvent.getJobId());
            NickleSchedulerSuccessJob nickleSchedulerSuccessJob = new NickleSchedulerSuccessJob();
            nickleSchedulerSuccessJob.setExecutorId(nickleSchedulerRunJob.getExecutorId());
            nickleSchedulerSuccessJob.setJobId(nickleSchedulerRunJob.getJobId());
            nickleSchedulerSuccessJob.setJobName(nickleSchedulerRunJob.getJobName());
            nickleSchedulerSuccessJob.setTriggerName(nickleSchedulerRunJob.getTriggerName());
            nickleSchedulerSuccessJob.setSuccessTime(System.currentTimeMillis());
            successJobMapper.insert(nickleSchedulerSuccessJob);
            this.getSender().tell(EXECUTOR_JOB_OK, getSelf());
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            log.error("任务完成处理器异常：{}", e.getMessage());
        } finally {
            sqlSession.close();
        }
    }


}
