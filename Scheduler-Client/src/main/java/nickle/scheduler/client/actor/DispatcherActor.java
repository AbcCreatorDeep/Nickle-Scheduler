package nickle.scheduler.client.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.routing.*;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import lombok.extern.slf4j.Slf4j;
import nickle.scheduler.client.event.ActiveJobEvent;
import nickle.scheduler.client.event.ClientRegisterEvent;
import nickle.scheduler.client.util.ClientUtils;
import nickle.scheduler.common.event.ExecuteJobEvent;
import nickle.scheduler.common.event.RegisterEvent;
import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static nickle.scheduler.client.actor.HeartBeatActor.HEART_BEAT;
import static nickle.scheduler.client.constant.Constants.ACTIVE_JOB_EVENT;
import static nickle.scheduler.common.Constant.*;

/**
 * 执行器分配器actor，接受任务调度的消息并启动任务
 *
 * @author nickle
 */
@Slf4j
public class DispatcherActor extends AbstractActor {
    private RegisterEvent registerEvent;
    private List<String> masterList;
    private volatile boolean registerSuccess = false;
    private ActorRef executorRouter;
    private static final int DEFAULT_EXECUTOR_NUM = Runtime.getRuntime().availableProcessors() * 2;

    private Router masterRouter;


    public static Props props() {
        return Props.create(DispatcherActor.class);
    }

    @Override
    public void preStart() {
        initMasterList();
        initMasterRouter();
        initExecutorRouter();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ExecuteJobEvent.class, this::execExecutorStartEvent)
                .match(ClientRegisterEvent.class, this::execRegister)
                .matchEquals(REGISTER_OK, this::registerReply)
                .matchEquals(ACTIVE_JOB_EVENT, this::execFindActiveJob).build();
    }

    /**
     * 初始化master str列表
     */
    private void initMasterList() {
        Config config = getContext().getSystem().settings().config();
        masterList = config.getStringList("remote.actor.path");
    }

    /**
     * 初始化执行器
     */
    private void initExecutorRouter() {
        log.info("执行器分配器启动");
        executorRouter = getContext().actorOf(ExecutorActor.props(masterRouter).withRouter(new RoundRobinPool(DEFAULT_EXECUTOR_NUM)),
                "ExecutorRouter");
        log.info("执行器分配器启动成功");
    }

    /**
     * 初始化master路由
     */
    private void initMasterRouter() {
        ArrayList<Routee> routeeList = Lists.newArrayList();
        for (String masterStr : masterList) {
            /**
             * master ip和端口号
             */
            String[] ipAndPort = masterStr.split(":");
            if (ObjectUtils.isEmpty(ipAndPort) || ipAndPort.length != 2) {
                log.error("ipAndPort 为空");
                ClientUtils.exit(getContext());
                return;
            }
            String path = String.format(AKKA_REMOTE_MODEL, SCHEDULER_SYSTEM_NAME, ipAndPort[0], ipAndPort[1], MASTER_MANAGER_ACTOR_NAME);
            routeeList.add(new ActorRefRoutee(getContext().actorFor(path)));
        }
        masterRouter = new Router(new RandomRoutingLogic(), routeeList);
    }

    /**
     * 执行注册
     *
     * @param clientRegisterEvent
     */
    private void execRegister(ClientRegisterEvent clientRegisterEvent) throws InterruptedException {
        Config config = getContext().getSystem().settings().config();
        //一直注册直到服务器返回数据
        if (!ObjectUtils.isEmpty(masterList)) {
            if (registerSuccess) {
                log.info("已注册成功");
                return;
            }
            ConfigObject configObject = config.getObject("akka.remote.netty.tcp");
            String hostname = configObject.get("hostname").unwrapped().toString();
            String portStr = configObject.get("port").unwrapped().toString();
            Integer port = Integer.valueOf(portStr);
            registerEvent = new RegisterEvent(hostname, port, clientRegisterEvent.getJobDataList(), clientRegisterEvent.getTriggerDataList());
            masterRouter.route(registerEvent, getSelf());
            //睡眠1s后注册，避免频繁注册
            Thread.sleep(1000);
            this.getSelf().tell(clientRegisterEvent, getSelf());
        } else {
            log.error("master地址为空,将不继续注册");
            ClientUtils.exit(getContext());
        }
    }


    /**
     * 注册返回信息
     *
     * @param msg
     */
    private void registerReply(Object msg) {
        if (REGISTER_OK.equals(msg)) {
            log.info("注册成功");
            registerSuccess = true;
            //发送信息给heartbeat启动心跳
            ActorRef actorRef = getContext().actorOf(HeartBeatActor.props(masterRouter, this.getSelf()), EXECUTOR_HEART_BEAT_NAME);
            actorRef.tell(HEART_BEAT, getSelf());
            //设置preMaster
            return;
        } else {
            log.info("注册失败,系统即将退出");
            ClientUtils.exit(getContext());
        }
    }


    /**
     * 接受到需调度的任务，分配到executor执行
     *
     * @param executeJobEvent
     */
    private void execExecutorStartEvent(ExecuteJobEvent executeJobEvent) {
        //返回接收成功给master
        /**
         * @// TODO: 2019/5/12 应实现背压，若所有执行器忙碌，则负载均衡
         */
        getSender().tell("OK", getSelf());
        executorRouter.tell(executeJobEvent, getSelf());
    }

    /**
     * @param msg
     * @// TODO: 2019/5/11 获取正在执行的job，待完成
     */
    private void execFindActiveJob(String msg) {
        ActiveJobEvent activeJobEvent = new ActiveJobEvent();
        activeJobEvent.setJobNameList(this.registerEvent.getJobDataList().stream().map(RegisterEvent.JobData::getJobName).collect(Collectors.toList()));
        getSender().tell(activeJobEvent, getSelf());
    }

}
