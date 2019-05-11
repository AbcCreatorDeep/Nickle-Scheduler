package nickle.scheduler.server.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lombok.extern.slf4j.Slf4j;
import nickle.scheduler.common.event.HeatBeatEvent;
import nickle.scheduler.common.event.RegisterEvent;
import org.apache.ibatis.session.SqlSessionFactory;

import static nickle.scheduler.common.Constant.*;

/**
 * 管理并转发请求
 *
 * @author nickle
 */
@Slf4j
public class MasterActor extends AbstractActor {
    private ActorRef heartBeatActor;
    private ActorRef registerActor;
    private ActorRef schedulerActor;
    private ActorRef executorCheckerActor;
    private SqlSessionFactory sqlSessionFactory;

    public static Props props(SqlSessionFactory sqlSessionFactory) {
        return Props.create(MasterActor.class, () -> new MasterActor(sqlSessionFactory));
    }

    public MasterActor(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * 初始化所有actor
     */
    @Override
    public void preStart() {
        schedulerActor = getContext().actorOf(SchedulerActor.props(sqlSessionFactory), SCHEDULER_SYSTEM_NAME);
        executorCheckerActor = getContext().actorOf(ExecutorCheckerActor.props(sqlSessionFactory), SCHEDULER_CHECKER_NAME);
        registerActor = getContext().actorOf(RegisterActor.props(sqlSessionFactory), SCHEDULER_REGISTER_NAME);
        heartBeatActor = getContext().actorOf(HeartBeatActor.props(sqlSessionFactory), SCHEDULER_HEART_BEAT_NAME);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RegisterEvent.class, registerEvent -> registerActor.tell(registerEvent, getSender()))
                .match(HeatBeatEvent.class, heatBeatEvent -> heartBeatActor.tell(heatBeatEvent, getSender()))
                .build();
    }
}
