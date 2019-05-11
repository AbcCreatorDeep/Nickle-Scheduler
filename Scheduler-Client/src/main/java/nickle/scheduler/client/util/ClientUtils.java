package nickle.scheduler.client.util;

import akka.actor.ActorContext;

/**
 * 工具类
 */
public class ClientUtils {
    public static void exit(ActorContext actorContext) {
        actorContext.system().terminate();
    }
}
