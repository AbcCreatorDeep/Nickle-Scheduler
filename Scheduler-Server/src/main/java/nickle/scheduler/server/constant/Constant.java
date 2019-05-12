package nickle.scheduler.server.constant;

/**
 * 服务端常量类
 */
public class Constant {
    /**
     * 等待执行
     */
    public static final Integer STAND_BY = 0;
    /**
     * 执行器已经获取
     */
    public static final Integer ACQUIRED = 1;
    /**
     * 触发器暂停
     */
    public static final Integer SUSPEND = 2;
    /**
     * 任务没有执行器状态
     */
    public static final Integer NO_EXECUTOR = 0;

    /**
     * 1：没有找到执行器，2：任务超时重调度, 3：所有执行器无法连接
     */
    public static final Byte NO_EXECUTOR_FAIL = 1;
    public static final Byte JOB_TIME_OUT = 2;
    public static final Byte EXECUTOR_NOT_CONNECTED = 3;
}
