package nickle.scheduler.test;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.OnComplete;
import akka.pattern.Patterns;
import akka.testkit.javadsl.TestKit;
import akka.util.Timeout;
import com.google.common.collect.Lists;
import nickle.scheduler.client.actor.DispatcherActor;
import nickle.scheduler.client.event.ClientRegisterEvent;
import nickle.scheduler.common.event.RegisterEvent;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static nickle.scheduler.common.Constant.EXECUTOR_DISPATCHER_NAME;
import static nickle.scheduler.common.Constant.EXECUTOR_SYSTEM_NAME;

/**
 * @author huangjun01
 * @title: ExecutorTest
 * @description:
 * @date 2019/5/6 15:15
 */
public class ExecutorTest {
    static ActorSystem system;
    private Object lock = new Object();

    static class ServerActor extends AbstractActor {
        public static Props props() {
            return Props.create(ServerActor.class);
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder().matchAny((msg) -> {
                Thread.sleep(2000);
                getSender().tell("ok", getSelf());
            }).build();
        }
    }

    static class ClientActor extends AbstractActor {
        public static Props props() {
            return Props.create(ClientActor.class);
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder().build();
        }
    }

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create(EXECUTOR_SYSTEM_NAME);
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testExecutor() {
        ActorRef server = system.actorOf(ServerActor.props());
        Timeout t = new Timeout(Duration.create(3, TimeUnit.SECONDS));
        //使用ask发送消息,actor处理完，必须有返回（超时时间5秒）
        try {
            Future<Object> ask = Patterns.ask(server, "123", t);
            ask.onComplete(new OnComplete<Object>() {
                @Override
                public void onComplete(Throwable throwable, Object o) throws Throwable {
                    if (throwable != null) {
                        System.out.println("some thing wrong.{}" + throwable);
                    } else {
                        System.out.println("success:" + o);
                    }
                }
            }, system.dispatcher());
            System.out.println("执行完毕");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRegister() throws IOException {
        ActorRef actorRef = system.actorOf(DispatcherActor.props(), EXECUTOR_DISPATCHER_NAME);
        ClientRegisterEvent jobDetail = new ClientRegisterEvent();
        ArrayList<RegisterEvent.JobData> jobDataArrayList = Lists.newArrayList();
        ArrayList<RegisterEvent.TriggerData> triggerDataArrayList = Lists.newArrayList();
        RegisterEvent.JobData jobData1 = new RegisterEvent.JobData();
        jobData1.setJobAuthor("nickle");
        jobData1.setJobClassName("nickle.scheduler.test.DemoJob1");
        jobData1.setJobDescription("测试job1");
        jobData1.setJobName("testJob1");
        jobData1.setJobTriggerName("testTrigger1");
        RegisterEvent.TriggerData triggerData1 = new RegisterEvent.TriggerData();
        triggerData1.setTriggerCron("*/5 * * * * ?");
        triggerData1.setTriggerName("testTrigger1");


        RegisterEvent.JobData jobData2 = new RegisterEvent.JobData();
        jobData2.setJobAuthor("nickle");
        jobData2.setJobClassName("nickle.scheduler.test.DemoJob2");
        jobData2.setJobDescription("测试job2");
        jobData2.setJobName("testJob2");
        jobData2.setJobTriggerName("testTrigger2");
        RegisterEvent.TriggerData triggerData2 = new RegisterEvent.TriggerData();
        triggerData2.setTriggerCron("*/5 * * * * ?");
        triggerData2.setTriggerName("testTrigger2");


        jobDataArrayList.add(jobData1);
        jobDataArrayList.add(jobData2);
        triggerDataArrayList.add(triggerData1);
        triggerDataArrayList.add(triggerData2);
        jobDetail.setJobDataList(jobDataArrayList);
        jobDetail.setTriggerDataList(triggerDataArrayList);
        actorRef.tell(jobDetail, actorRef);
        System.in.read();
    }
}
