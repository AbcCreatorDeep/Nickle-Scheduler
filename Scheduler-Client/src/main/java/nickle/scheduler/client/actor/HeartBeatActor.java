package nickle.scheduler.client.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.Router;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import lombok.extern.slf4j.Slf4j;
import nickle.scheduler.client.event.ActiveJobEvent;
import nickle.scheduler.common.event.HeatBeatEvent;

import static nickle.scheduler.client.constant.Constants.ACTIVE_JOB_EVENT;

/**
 * @author nickle
 * @description:
 * @date 2019/5/8 13:20
 */
@Slf4j
public class HeartBeatActor extends AbstractActor {
    public static final String HEART_BEAT = "HEART_BEAT";
    /**
     * 3s发一次心跳
     */
    private static final long EXECUTOR_HEART_BEAT_INTERVAL = 3 * 1000L;

    public static Props props(Router masterRouter, ActorRef dispatcherActor) {
        return Props.create(HeartBeatActor.class, () -> new HeartBeatActor(masterRouter, dispatcherActor));
    }

    private Router masterRouter;
    private String localHostName;
    private Integer localPort;
    private ActorRef dispatcherActor;

    public HeartBeatActor(Router masterRouter, ActorRef dispatcherActor) {
        Config config = getContext().getSystem().settings().config();
        ConfigObject configObject = config.getObject("akka.remote.netty.tcp");
        localHostName = configObject.get("hostname").unwrapped().toString();
        localPort = Integer.valueOf(configObject.get("port").unwrapped().toString());
        this.masterRouter = masterRouter;
        this.dispatcherActor = dispatcherActor;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .matchEquals(HEART_BEAT, this::startHeartBeat)
                .match(ActiveJobEvent.class, this::heartBeatToMaster)
                .build();
    }

    private void heartBeatToMaster(ActiveJobEvent activeJobEvent) {
        HeatBeatEvent heatBeatEvent = new HeatBeatEvent(localHostName, localPort, activeJobEvent.getJobNameList());
        masterRouter.route(heatBeatEvent, getSelf());
        nextHeart();
    }

    private void startHeartBeat(String msg) {
        dispatcherActor.tell(ACTIVE_JOB_EVENT, this.getSelf());
    }

    private void nextHeart() {
        try {
            Thread.sleep(EXECUTOR_HEART_BEAT_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getSelf().tell(HEART_BEAT, getSelf());
    }
}
