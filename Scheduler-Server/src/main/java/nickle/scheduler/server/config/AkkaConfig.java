package nickle.scheduler.server.config;

import akka.actor.ActorSystem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static nickle.scheduler.common.Constant.SCHEDULER_SYSTEM_NAME;

/**
 * @author nickle
 * @description:
 * @date 2019/5/7 15:04
 */
@Configuration
public class AkkaConfig {
    @Bean(destroyMethod = "terminate")
    public ActorSystem actorSystem() {
        return ActorSystem.create(SCHEDULER_SYSTEM_NAME);
    }
}
