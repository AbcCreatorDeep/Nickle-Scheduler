package nickle.scheduler.test;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import nickle.scheduler.client.actor.ExecutorActor;
import nickle.scheduler.common.ExecutorStartEvent;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author huangjun01
 * @title: ExecutorTest
 * @description:
 * @date 2019/5/6 15:15
 */
public class ExecutorTest {
    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testExecutor() {
        new TestKit(system) {
            {
                ActorRef test_executor = system.actorOf(ExecutorActor.props(), "test-executor");
                // can also use JavaTestKit “from the outside”
                final TestKit probe = new TestKit(system);
                ExecutorStartEvent executorStartEvent = new ExecutorStartEvent();
                executorStartEvent.setClassName("nickle.scheduler.test.DemoJob");
                executorStartEvent.setSpliceNum(10);
                test_executor.tell(executorStartEvent, Actor.noSender());
                // await the correct response
                //expectMsg(Duration.ofSeconds(1), "done");

                // the run() method needs to finish within 3 seconds
//                within(
//                        Duration.ofSeconds(3),
//                        () -> {
//                            subject.tell("hello", getRef());
//
//                            // This is a demo: would normally use expectMsgEquals().
//                            // Wait time is bounded by 3-second deadline above.
//                            awaitCond(probe::msgAvailable);
//
//                            // response must have been enqueued to us before probe
//                            expectMsg(Duration.ZERO, "world");
//                            // check that the probe we injected earlier got the msg
//                            probe.expectMsg(Duration.ZERO, "hello");
//                            Assert.assertEquals(getRef(), probe.getLastSender());
//
//                            // Will wait for the rest of the 3 seconds
//                            expectNoMessage();
//                            return null;
//                        });
            }
        };

    }
}
