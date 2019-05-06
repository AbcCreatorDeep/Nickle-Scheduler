package nickle.scheduler.server.core.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author huangjun01
 * @title: SimpleJob
 * @description:
 * @date 2019/5/6 16:45
 */
public class SimpleJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

    }
}
