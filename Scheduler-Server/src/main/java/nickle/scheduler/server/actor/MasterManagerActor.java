package nickle.scheduler.server.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.RandomPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;

import static nickle.scheduler.common.Constant.MASTER_MANAGER_ACTOR_NAME;

/**
 * 启动多个Master，负载均衡
 *
 * @author nickle
 */
@Slf4j
public class MasterManagerActor extends AbstractActor {
    private ActorRef masterRouter;
    private SqlSessionFactory sqlSessionFactory;
    public static final int DEFAULT_MASTER_NUM = 2;

    public static Props props(SqlSessionFactory sqlSessionFactory) {
        return Props.create(MasterManagerActor.class, () -> new MasterManagerActor(sqlSessionFactory));
    }

    public MasterManagerActor(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * 初始化所有actor
     */
    @Override
    public void preStart() {
        masterRouter = getContext().actorOf(MasterActor.props(sqlSessionFactory).withRouter(new RandomPool(DEFAULT_MASTER_NUM)), MASTER_MANAGER_ACTOR_NAME);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().matchAny(event -> masterRouter.tell(event, getSender())).build();
    }
}
