package nickle.scheduler.server.util;

import nickle.scheduler.common.event.RegisterEvent;
import nickle.scheduler.server.entity.NickleSchedulerJob;
import nickle.scheduler.server.entity.NickleSchedulerTrigger;
import org.springframework.cglib.beans.BeanCopier;

/**
 * @author nickle
 * @description:
 * @date 2019/5/8 11:01
 */
public class BeanCopiers {
    public static final BeanCopier JOBDATA_COPY_TO_JOB = BeanCopier.create(RegisterEvent.JobData.class, NickleSchedulerJob.class, false);
    public static final BeanCopier TIRGGERDATA_COPY_TO_TIRGGER = BeanCopier.create(RegisterEvent.TriggerData.class, NickleSchedulerTrigger.class, false);
}
