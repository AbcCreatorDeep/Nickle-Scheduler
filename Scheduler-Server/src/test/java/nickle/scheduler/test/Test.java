package nickle.scheduler.test;

import nickle.scheduler.common.event.RegisterEvent;
import nickle.scheduler.server.entity.NickleSchedulerJob;
import org.springframework.cglib.beans.BeanCopier;

/**
 * @author nickle
 * @description:
 * @date 2019/5/7 18:20
 */
public class Test {
    public static void main(String[] args) {
        NickleSchedulerJob nickleSchedulerJob = new NickleSchedulerJob();
        RegisterEvent.JobData jobData = new RegisterEvent.JobData();
        jobData.setJobName("1231");
        BeanCopier JOBDATA_COPY_TO_JOB = BeanCopier.create(RegisterEvent.JobData.class, NickleSchedulerJob.class, false);
        JOBDATA_COPY_TO_JOB.copy(jobData, nickleSchedulerJob, null);
        System.out.println(nickleSchedulerJob);
    }
}
