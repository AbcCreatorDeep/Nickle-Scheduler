package nickle.scheduler.client.actor;


import akka.actor.AbstractActor;
import akka.actor.Props;
import lombok.extern.slf4j.Slf4j;
import nickle.scheduler.client.core.SafeJob;
import nickle.scheduler.client.core.SchedulerJob;
import nickle.scheduler.common.event.ExecuteJobEvent;
import nickle.scheduler.common.event.ExecuteResultEvent;

@Slf4j
public class ExecutorActor extends AbstractActor {
    public static Props props() {
        return Props.create(ExecutorActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ExecuteJobEvent.class, executeJobEvent -> {
                    executeJob(executeJobEvent);
                }).build();
    }

    private void executeJob(ExecuteJobEvent executeJobEvent) throws Exception {
        log.info("执行器收到任务：{}", executeJobEvent);
        String className = executeJobEvent.getClassName();
        Class<?> aClass = Class.forName(className);
        SchedulerJob job = (SchedulerJob) aClass.newInstance();
        SafeJob safeJob = new SafeJob(job);
        safeJob.execute(null);
        ExecuteResultEvent executeResultEvent = new ExecuteResultEvent();
        executeResultEvent.setThrowable(safeJob.getExeception());
        getSender().tell(executeResultEvent, this.getSelf());
    }

}
