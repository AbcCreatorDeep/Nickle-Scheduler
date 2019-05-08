package nickle.scheduler.client.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import lombok.extern.slf4j.Slf4j;
import nickle.scheduler.common.event.HeatBeatEvent;

import static nickle.scheduler.common.Constant.*;

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

    public static Props props(String remoteIp, String remotePort, String localIp, String localPort) {
        return Props.create(HeartBeatActor.class, () -> new HeartBeatActor(remoteIp, remotePort, localIp, localPort));
    }

    private String remoteIp;
    private Integer remotePort;
    private String localIp;
    private Integer localPort;

    public HeartBeatActor(String remoteIp, String remotePort, String localIp, String localPort) {
        this.remoteIp = remoteIp;
        this.remotePort = Integer.valueOf(remotePort);
        this.localIp = localIp;
        this.localPort = Integer.valueOf(localPort);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchEquals(HEART_BEAT, this::heartBeat).build();
    }

    private void heartBeat(String msg) {
        //发送心跳给master的heartbeat
        String path = String.format(AKKA_REMOTE_MODEL, SCHEDULER_SYSTEM_NAME, remoteIp, remotePort, SCHEDULER_HEART_BEAT_NAME);
        ActorSelection actorSelection = getContext().actorSelection(path);
        HeatBeatEvent heatBeatEvent = new HeatBeatEvent();
        heatBeatEvent.setIp(localIp);
        heatBeatEvent.setPort(localPort);
        actorSelection.tell(heatBeatEvent, getSelf());
        try {
            Thread.sleep(EXECUTOR_HEART_BEAT_INTERVAL);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getSelf().tell(HEART_BEAT, getSelf());
    }

}
