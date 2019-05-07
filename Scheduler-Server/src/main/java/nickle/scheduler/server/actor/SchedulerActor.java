package nickle.scheduler.server.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;


/**
 * @author nickle
 * @description:
 * @date 2019/5/7 14:59
 */
public class SchedulerActor extends AbstractActor {
    private int scheduleTime = 10 * 1000;

    public static Props props() {
        return Props.create(SchedulerActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchAny(msg -> cycleSchedule()).build();
    }

    private void cycleSchedule() {
        System.out.println(1);
        try {
            Thread.sleep(scheduleTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getSender().tell("", getSelf());
    }
}
