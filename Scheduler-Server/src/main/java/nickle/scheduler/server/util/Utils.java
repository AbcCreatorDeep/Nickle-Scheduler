package nickle.scheduler.server.util;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import nickle.scheduler.common.event.ExecuteJobEvent;
import nickle.scheduler.server.entity.*;
import nickle.scheduler.server.mapper.NickleSchedulerExecutorJobMapper;
import nickle.scheduler.server.mapper.NickleSchedulerExecutorMapper;
import nickle.scheduler.server.mapper.NickleSchedulerFailJobMapper;
import nickle.scheduler.server.mapper.NickleSchedulerRunJobMapper;
import org.apache.ibatis.session.SqlSession;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

import static nickle.scheduler.common.Constant.*;

/**
 * @author nickle
 * @description:
 * @date 2019/5/8 10:17
 */
@Slf4j
public class Utils {
    public static <T> void insertBatch(Collection<T> entityList, SqlSession sqlSession) {

    }

    public static void scheduleJob(List<NickleSchedulerJob> nickleSchedulerRunJobs,
                                   SqlSession sqlSession,
                                   ActorContext actorContext,
                                   ActorRef sender) {
        NickleSchedulerRunJobMapper runJobMapper = sqlSession.getMapper(NickleSchedulerRunJobMapper.class);
        NickleSchedulerExecutorMapper executorMapper = sqlSession.getMapper(NickleSchedulerExecutorMapper.class);
        NickleSchedulerExecutorJobMapper executorJobMapper = sqlSession.getMapper(NickleSchedulerExecutorJobMapper.class);
        NickleSchedulerFailJobMapper failJobMapper = sqlSession.getMapper(NickleSchedulerFailJobMapper.class);
        log.info("需要调度的job：{}", nickleSchedulerRunJobs);
        for (NickleSchedulerJob job : nickleSchedulerRunJobs) {
            //获取到job对应执行器，选取发送
            QueryWrapper<NickleSchedulerExecutorJob> executorJobQueryWrapper = new QueryWrapper<>();
            executorJobQueryWrapper.lambda().eq(NickleSchedulerExecutorJob::getJobId, job.getId());
            List<NickleSchedulerExecutorJob> nickleSchedulerExecutorJobs = executorJobMapper.selectList(executorJobQueryWrapper);
            if (CollectionUtils.isEmpty(nickleSchedulerExecutorJobs)) {
                //此时该job的执行器为空，待处理
                log.error("当前任务没有可用执行器:{},已记录到失败表中", job);
                //记录到失败表中
                NickleSchedulerFailJob nickleSchedulerFailJob = new NickleSchedulerFailJob();
                //如果第一次调度时保存将没有执行器
                nickleSchedulerFailJob.setExecutorId(0);
                nickleSchedulerFailJob.setJobName(job.getJobName());
                nickleSchedulerFailJob.setFailReason((byte) 1);
                nickleSchedulerFailJob.setTriggerName(job.getJobTriggerName());
                nickleSchedulerFailJob.setFailedTime(System.currentTimeMillis());
                nickleSchedulerFailJob.setJobId(job.getId());
                failJobMapper.insert(nickleSchedulerFailJob);
                continue;
            } else {
                //调度任务，默认取第一个执行器
                NickleSchedulerExecutorJob nickleSchedulerExecutorJob = nickleSchedulerExecutorJobs.get(0);
                QueryWrapper<NickleSchedulerExecutor> schedulerExecutorQueryWrapper = new QueryWrapper<>();
                schedulerExecutorQueryWrapper.lambda().eq(NickleSchedulerExecutor::getExecutorId, nickleSchedulerExecutorJob.getExecutorId());
                NickleSchedulerExecutor nickleSchedulerExecutor = executorMapper.selectOne(schedulerExecutorQueryWrapper);
//                //如果没有可用执行器则打印错误日志
//                if (nickleSchedulerExecutor == null) {
//                    log.error("没有找到可用执行器:{}", job);
//                    continue;
//                }
                //拼接获取远程actor发送信息
                ActorSelection actorSelection = actorContext.actorSelection(String.format(AKKA_REMOTE_MODEL
                        , nickleSchedulerExecutor.getExecutorIp()
                        , nickleSchedulerExecutor.getExecutorPort()
                        , EXECUTOR_SYSTEM_NAME
                        , EXECUTOR_DISPATCHER_NAME));
                ExecuteJobEvent executorStartEvent = new ExecuteJobEvent();
                executorStartEvent.setClassName(job.getJobClassName());
                log.info("开始调度job：{}", job);
                actorSelection.tell(executorStartEvent, sender);
                //插入运行队列
                NickleSchedulerRunJob nickleSchedulerRunJob = new NickleSchedulerRunJob();
                nickleSchedulerRunJob.setExecutorId(nickleSchedulerExecutorJob.getExecutorId());
                nickleSchedulerRunJob.setJobName(job.getJobName());
                nickleSchedulerRunJob.setJobId(job.getId());
                nickleSchedulerRunJob.setScheduleTime(System.currentTimeMillis());
                nickleSchedulerRunJob.setTriggerName(job.getJobTriggerName());
                nickleSchedulerRunJob.setUpdateTime(System.currentTimeMillis());
                runJobMapper.insert(nickleSchedulerRunJob);
            }
        }
    }


}
