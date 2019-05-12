package nickle.scheduler.common;

/**
 * @author huangjun01
 * @title: Constant
 * @description: client and server used constant
 * @date 2019/5/6 14:43
 */
public class Constant {
    /**
     * akka相关
     */
    public static final String AKKA_ROUTE_SPLIT = "/";
    public static final String AKKA_REMOTE_MODEL = "akka.tcp://%s@%s:%s/user/%s";
    public static final String AKKA_LOCAL_PATH_MODEL = "akka://%s/user/%s";
    /**
     * 调度器配置
     */
    public static final String SCHEDULER_SYSTEM_NAME = "schedulerSystem";
    public static final String MASTER_ACTOR_NAME = "masterActor";
    public static final String MASTER_COMPLETE_JOB_ACTOR_NAME = "completeJobActor";
    public static final String MASTER_MANAGER_ACTOR_NAME = "masterManagerActor";
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
    /**
     * 注册后用于回复注册成功
     */
    public static final String REGISTER_FAIL = "REGISTER_FAIL";
    /**
     * 用于Master回复心跳成功
     */
    public static final String HEARTBEAT_OK = "HEARTBEAT_OK";
    /**
     * 任务完成后用于回复master处理成功
     */
    public static final String EXECUTOR_JOB_OK = "EXECUTOR_JOB_OK";
    /**
     * ip:port 模板
     */
    public static final String SOCKET_ADDRESS_MODEL = "%s:%d";
}
