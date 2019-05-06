package nickle.scheduler.common;

/**
 * @author huangjun01
 * @title: Lifecyle
 * @description:
 * @date 2019/5/6 16:16
 */
public interface SchedulerLifecyle {
    void init() throws Exception;

    void start() throws Exception;

    void stop() throws Exception;
}
