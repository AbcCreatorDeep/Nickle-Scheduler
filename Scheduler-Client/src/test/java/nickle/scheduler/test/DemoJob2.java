package nickle.scheduler.test;

import nickle.scheduler.client.core.SchedulerContext;
import nickle.scheduler.client.core.SchedulerJob;

/**
 * @author huangjun01
 * @title: DemoJob1
 * @description:
 * @date 2019/5/6 15:16
 */
public class DemoJob2 implements SchedulerJob {
    @Override
    public void execute(SchedulerContext schedulerContext) throws Exception {
        System.out.println("任务2执行");
    }
}
