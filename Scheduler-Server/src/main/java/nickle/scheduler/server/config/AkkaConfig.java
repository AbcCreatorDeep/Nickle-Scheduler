package nickle.scheduler.server.config;

import akka.actor.ActorSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author nickle
 * @description:
 * @date 2019/5/7 15:04
 */
@Configuration
public class AkkaConfig {
    @Bean
    public ActorSystem actorSystem() {
        return ActorSystem.create("nickle-scheduler-server-as");
    }
}
