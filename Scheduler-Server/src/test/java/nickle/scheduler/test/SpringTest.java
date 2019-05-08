package nickle.scheduler.test; /**
 * @description:
 * @author huangjun01
 * @date 2019/5/7 14:52
 */

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import nickle.scheduler.server.Application;
import nickle.scheduler.server.actor.CheckActor;
import nickle.scheduler.server.actor.HeartBeatActor;
import nickle.scheduler.server.actor.RegisterActor;
import nickle.scheduler.server.actor.SchedulerActor;

import nickle.scheduler.server.entity.NickleSchedulerExecutor;
import nickle.scheduler.server.mapper.NickleSchedulerExecutorMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static nickle.scheduler.common.Constant.*;

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
    @Autowired
    private NickleSchedulerExecutorMapper executorMapper;

    @Test
    public void testActorSystem() throws InterruptedException {
        ActorRef schedulerActor = actorSystem.actorOf(SchedulerActor.props(sqlSessionFactory), SCHEDULER_SYSTEM_NAME);
        ActorRef checkActor = actorSystem.actorOf(CheckActor.props(sqlSessionFactory), SCHEDULER_CHECKER_NAME);
        ActorRef registerActor = actorSystem.actorOf(RegisterActor.props(sqlSessionFactory), SCHEDULER_REGISTER_NAME);
        ActorRef heartBeatActor = actorSystem.actorOf(HeartBeatActor.props(sqlSessionFactory), SCHEDULER_HEART_BEAT_NAME);
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            actorSystem.terminate();
        }
    }

    @Test
    public void testMapper() {
        NickleSchedulerExecutor nickleSchedulerExecutor = new NickleSchedulerExecutor();
        nickleSchedulerExecutor.setExecutorIp("123.123.123.123");
        nickleSchedulerExecutor.setExecutorPort(8080);
        nickleSchedulerExecutor.setUpdateTime(System.currentTimeMillis());
        System.out.println(executorMapper.insert(nickleSchedulerExecutor));
        System.out.println(nickleSchedulerExecutor.getExecutorId());
    }
}
