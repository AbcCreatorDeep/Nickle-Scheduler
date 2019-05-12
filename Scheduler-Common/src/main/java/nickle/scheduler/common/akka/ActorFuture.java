package nickle.scheduler.common.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorContext;
import akka.actor.Props;
import akka.routing.Router;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static nickle.scheduler.common.Constant.EXECUTOR_JOB_OK;

/**
 * 包装ActorRef或Router，返回java的future
 */
@Slf4j
public class ActorFuture {
    private static final int DEFAULT_THREAD_NUM = 50;
    private static final String THREAD_NAME = "Actor-Future-Thread";
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(1);
    /**
     * 固定线程池
     */
    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(DEFAULT_THREAD_NUM, DEFAULT_THREAD_NUM,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, THREAD_NAME + ATOMIC_INTEGER.getAndIncrement());
        }
    });

    /**
     * 返回java的future
     *
     * @param router
     * @param msg
     * @param actorContext
     * @return
     */
    public static Future<Object> ask(Router router, Object msg, ActorContext actorContext) {
        return THREAD_POOL.submit(new SenderFutureTask(router, msg, actorContext));
    }

    @Slf4j
    static class SenderFutureTask implements Callable<Object> {
        private Object msg;
        private Router router;
        ActorContext actorContext;
        private Object lock = new Object();
        private Object result;

        private SenderFutureTask(Router router, Object msg, ActorContext actorContext) {
            this.router = router;
            this.msg = msg;
            this.actorContext = actorContext;
        }

        @Override
        public Object call() throws Exception {
            /**
             * 创建actor发送信息，直到成功
             */
            log.info("xxxxxxxxxxxxxxxxxxxxxxxxxx");
            actorContext.actorOf(SenderActor.props(router, msg, this));
            synchronized (lock) {
                lock.wait();
            }
            return result;
        }
    }

    @Slf4j
    static class SenderActor extends AbstractActor {
        private Object msg;
        private Router router;
        private SenderFutureTask senderFutureTask;
        private static final String SENDER = "SENDER";

        public SenderActor(Router router, Object msg, SenderFutureTask senderFutureTask) {
            this.router = router;
            this.msg = msg;
            this.senderFutureTask = senderFutureTask;
        }

        public static Props props(Router router, Object msg, SenderFutureTask senderFutureTask) {
            return Props.create(SenderActor.class, router, msg, senderFutureTask);
        }

        @Override
        public void preStart() {
            nextSend();
        }

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .matchEquals(EXECUTOR_JOB_OK, this::success)
                    .matchEquals(SENDER, this::executeSend)
                    .build();
        }

        /**
         * 成功后回调
         *
         * @param msg
         */
        private void success(Object msg) {
            senderFutureTask.result = msg;
            synchronized (senderFutureTask.lock) {
                senderFutureTask.lock.notify();
            }
            log.info("任务发送到master成功，信息为：{}", msg);
            senderFutureTask.result = msg;
            /**
             * 停止这个actor
             */
            getContext().stop(this.getSelf());
            return;
        }

        /**
         * 执行发送逻辑，不断重复直到发送成功
         *
         * @param msg
         */
        private void executeSend(Object msg) {
            router.route(this.msg, this.getSelf());
        }

        private void nextSend() {
            getSelf().tell(SENDER, getSelf());
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.info("发送任务执行状态出错：{}", e.getMessage());
            }
        }
    }
}
