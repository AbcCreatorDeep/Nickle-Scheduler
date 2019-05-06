package nickle.scheduler.server.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import nickle.scheduler.common.ExecutorSubmitJobEvent;

/**
 * @author huangjun01
 * @title: SubmitJobActor
 * @description:
 * @date 2019/5/6 18:00
 */
public class SubmitJobActor extends AbstractActor {
    public static Props props(ActorRef quartzActorRef) {
        return Props.create(SubmitJobActor.class, () -> new SubmitJobActor(quartzActorRef));
    }

    private ActorRef quartzActorRef;

    public SubmitJobActor(ActorRef quartzActorRef) {
        this.quartzActorRef = quartzActorRef;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ExecutorSubmitJobEvent.class, this::execExecutorSubmitJobEvent).build();
    }

    private void execExecutorSubmitJobEvent(ExecutorSubmitJobEvent executorSubmitJobEvent) {
        //权限检测等


        //提交任务

    }
}
