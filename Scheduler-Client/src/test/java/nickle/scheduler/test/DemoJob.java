package nickle.scheduler.test;

import nickle.scheduler.client.core.SchedulerContext;
import nickle.scheduler.client.core.SchedulerJob;

/**
 * @author huangjun01
 * @title: DemoJob
 * @description:
 * @date 2019/5/6 15:16
 */
public class DemoJob implements SchedulerJob {
    @Override
    public void execute(SchedulerContext schedulerContext) throws Exception {
        System.out.println("receive msg,分片数" + schedulerContext.getSpliceNum());
    }
}
