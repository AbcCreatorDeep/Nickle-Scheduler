package nickle.scheduler.client.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.util.Timeout;
import lombok.extern.slf4j.Slf4j;
import nickle.scheduler.client.event.ActiveJobEvent;
import nickle.scheduler.common.event.HeatBeatEvent;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static nickle.scheduler.client.constant.Constants.ACTIVE_JOB_EVENT;
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
        Timeout t = new Timeout(Duration.create(1, TimeUnit.SECONDS));
        Future<Object> ask = Patterns.ask(getContext().actorSelection(String.format(AKKA_LOCAL_PATH_MODEL, EXECUTOR_SYSTEM_NAME, EXECUTOR_DISPATCHER_NAME)), ACTIVE_JOB_EVENT, t);
        ask.onComplete(new OnComplete<Object>() {
            @Override
            public void onComplete(Throwable throwable, Object obj) throws Throwable {
                if (throwable != null) {
                    log.error("连接超时");
                } else {
                    ActiveJobEvent event = (ActiveJobEvent) obj;
                    ActorSelection actorSelection = getContext().actorSelection(path);
                    HeatBeatEvent heatBeatEvent = new HeatBeatEvent();
                    heatBeatEvent.setIp(localIp);
                    heatBeatEvent.setPort(localPort);
                    heatBeatEvent.setJobNameList(event.getJobNameList());
                    actorSelection.tell(heatBeatEvent, getSelf());
                }
            }
        }, getContext().dispatcher());
        nextHeart();
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
