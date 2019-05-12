package nickle.scheduler.client.actor;


import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.routing.Router;
import lombok.extern.slf4j.Slf4j;
import nickle.scheduler.client.core.SafeJob;
import nickle.scheduler.client.core.SchedulerJob;
import nickle.scheduler.common.akka.ActorFuture;
import nickle.scheduler.common.event.ExecuteJobEvent;
import nickle.scheduler.common.event.ExecuteResultEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static nickle.scheduler.common.Constant.EXECUTOR_JOB_OK;

/**
 * 任务执行器
 *
 * @author nickle
 */
@Slf4j
public class ExecutorActor extends AbstractActor {
    private Router masterRouter;

    public static Props props(Router masterRouter) {
        return Props.create(ExecutorActor.class, masterRouter);
    }

    public ExecutorActor(Router masterRouter) {
        this.masterRouter = masterRouter;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ExecuteJobEvent.class, this::executeJob).build();
    }

    /**
     * 执行任务的执行器
     *
     * @param executeJobEvent
     * @throws Exception
     */
    private void executeJob(ExecuteJobEvent executeJobEvent) throws Exception {
        log.info("执行器收到任务：{}", executeJobEvent);
        String className = executeJobEvent.getClassName();
        Class<?> aClass = Class.forName(className);
        SchedulerJob job = (SchedulerJob) aClass.newInstance();
        SafeJob safeJob = new SafeJob(job);
        safeJob.execute(null);
        ExecuteResultEvent executeResultEvent = new ExecuteResultEvent();
        executeResultEvent.setRunJobId(executeJobEvent.getRunJobId());
        executeResultEvent.setThrowable(safeJob.getExeception());
        notifyMaster(executeResultEvent);
    }

    /**
     * 执行器通知master,直到成功
     */
    private void notifyMaster(ExecuteResultEvent executeResultEvent) throws ExecutionException, InterruptedException {
        Future<Object> future = ActorFuture.ask(masterRouter, executeResultEvent, getContext());
        while (!EXECUTOR_JOB_OK.equals(future.get())) {
            future = ActorFuture.ask(masterRouter, executeResultEvent, getContext());
        }
    }
}
