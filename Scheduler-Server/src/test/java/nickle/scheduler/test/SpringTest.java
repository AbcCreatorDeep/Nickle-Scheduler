package nickle.scheduler.test; /**
 * @description:
 * @author huangjun01
 * @date 2019/5/7 14:52
 */

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import nickle.scheduler.server.Application;
import nickle.scheduler.server.actor.SchedulerActor;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

/**
 * @author huangjun01
 * @title: SpringTest
 * @description:
 * @date 2019/5/7 14:52
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})// 指定启动类
public class SpringTest {
    @Autowired
    private ActorSystem actorSystem;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
    public void testActorSystem() throws InterruptedException {
        ActorRef actorRef = actorSystem.actorOf(SchedulerActor.props(sqlSessionFactory));
        actorRef.tell("", actorRef);
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            actorSystem.terminate();
        }
    }
}
