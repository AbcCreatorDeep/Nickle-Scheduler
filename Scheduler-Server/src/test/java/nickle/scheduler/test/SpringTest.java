package nickle.scheduler.test; /**
 * @description:
 * @author huangjun01
 * @date 2019/5/7 14:52
 */

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import nickle.scheduler.server.Application;
import nickle.scheduler.server.actor.*;

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
        ActorRef masterManagerActor = actorSystem.actorOf(MasterManagerActor.props(sqlSessionFactory), MASTER_MANAGER_ACTOR_NAME);
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
