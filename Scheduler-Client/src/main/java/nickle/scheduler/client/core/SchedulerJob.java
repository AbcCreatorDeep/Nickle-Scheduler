package nickle.scheduler.client.core;

/**
 * @author huangjun01
 * @title: SchedulerJob
 * @description:
 * @date 2019/5/6 14:59
 */
public interface SchedulerJob {
    void execute(SchedulerContext schedulerContext) throws Exception;
}
