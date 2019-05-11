package nickle.scheduler.client.core;

import lombok.Data;

/**
 * 包装执行的任务，捕捉所有异常
 *
 * @author nickle
 */
@Data
public class SafeJob {
    private SchedulerJob schedulerJob;
    private Throwable exeception;

    public SafeJob(SchedulerJob schedulerJob) {
        this.schedulerJob = schedulerJob;
    }

    /**
     * 真正执行的job
     */
    public void execute(SchedulerContext schedulerContext) {
        try {
            schedulerJob.execute(schedulerContext);
        } catch (Throwable throwable) {
            this.exeception = throwable;
        }
    }
}
