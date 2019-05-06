package nickle.scheduler.client.actor;


import akka.actor.AbstractActor;
import akka.actor.Props;
import nickle.scheduler.common.SchedulerLifecyle;

public class ClientActor extends AbstractActor implements SchedulerLifecyle {
    public static Props props() {
        return Props.create(ClientActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().build();
    }

    @Override
    public void init() throws Exception {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {

    }
}
