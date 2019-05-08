package nickle.scheduler.common;

/**
 * @author huangjun01
 * @title: Constant
 * @description: client and server used constant
 * @date 2019/5/6 14:43
 */
public class Constant {
    public static final String AKKA_ROUTE_SPLIT = "/";
    public static final String AKKA_REMOTE_MODEL = "akka.tcp://%s@%s:%s/user/%s";
    /**
     * 调度器配置
     */
    public static final String SCHEDULER_SYSTEM_NAME = "schedulerSystem";
    public static final String SCHEDULER_REGISTER_NAME = "schedulerRegisterActor";
    public static final String SCHEDULER_CHECKER_NAME = "schedulerCheckerActor";
    public static final String SCHEDULER_HEART_BEAT_NAME = "schedulerHeartBeatActor";
    /**
     * 执行器配置
     */
    public static final String EXECUTOR_DISPATCHER_NAME = "dispatcherActor";
    public static final String EXECUTOR_SYSTEM_NAME = "executorSystem";
    public static final String EXECUTOR_HEART_BEAT_NAME = "executorHeartBeatActor";
    /**
     * 注册后用于回复注册成功
     */
    public static final String REGISTER_OK = "REGISTER_OK";
}
