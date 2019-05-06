package nickle.scheduler.client.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.typesafe.config.ConfigFactory;
import nickle.scheduler.client.core.SchedulerContext;
import nickle.scheduler.client.core.SchedulerJob;
import nickle.scheduler.common.ExecutorStartEvent;
import nickle.scheduler.common.NickleSchedulerExeception;

/**
 * 执行器注册actor
 */
public class ExecutorActor extends AbstractActor {
    public static Props props() {
        return Props.create(ExecutorActor.class);
    }

    @Override
    public Receive createReceive() {
        System.out.println(ConfigFactory.defaultApplication().getString("remote.actor.name"));
        return receiveBuilder()
                .match(ExecutorStartEvent.class, wtg -> {
                    execExecutorStartEvent(wtg);
                }).build();
    }

    private void execExecutorStartEvent(ExecutorStartEvent executorStartEvent) {

        String className = executorStartEvent.getClassName();
        try {
            Object o = Class.forName(className).newInstance();
            if (!(o instanceof SchedulerJob)) {
                throw new NickleSchedulerExeception("job must implement SchedulerJob interface");
            }
            SchedulerJob schedulerJob = (SchedulerJob) o;
            SchedulerContext schedulerContext = new SchedulerContext();
            schedulerContext.setSpliceNum(executorStartEvent.getSpliceNum());
            schedulerJob.execute(schedulerContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
