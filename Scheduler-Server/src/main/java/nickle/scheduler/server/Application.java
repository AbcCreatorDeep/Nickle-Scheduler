package nickle.scheduler.server;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import nickle.scheduler.server.actor.MasterManagerActor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static nickle.scheduler.common.Constant.MASTER_MANAGER_ACTOR_NAME;

/**
 * 应用启动类
 *
 * @author nickle
 */
@SpringBootApplication
@MapperScan("nickle.scheduler.server.mapper")
@Component
public class Application {
    @Autowired
    private ActorSystem actorSystem;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    private ActorRef masterManagerActor;
    @PostConstruct
    private void start() {
        masterManagerActor = actorSystem.actorOf(MasterManagerActor.props(sqlSessionFactory), MASTER_MANAGER_ACTOR_NAME);

    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
