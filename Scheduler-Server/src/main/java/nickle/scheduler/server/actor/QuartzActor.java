package nickle.scheduler.server.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import nickle.scheduler.common.ExecutorSubmitJobEvent;
import nickle.scheduler.common.SchedulerLifecyle;
import nickle.scheduler.server.core.job.SimpleJob;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

/**
 * @author huangjun01
 * @title: QuartzActor
 * @description:
 * @date 2019/5/6 16:14
 */
public class QuartzActor extends AbstractActor implements SchedulerLifecyle {
    private Scheduler scheduler;

    public static Props props() {
        return Props.create(QuartzActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ExecutorSubmitJobEvent.class, this::execExecutorSubmitJobEvent).build();
    }

    private void execExecutorSubmitJobEvent(ExecutorSubmitJobEvent executorRegistEvent) {
        JobDetail jobDetail = JobBuilder.newJob(SimpleJob.class)
                .withIdentity("job", Scheduler.DEFAULT_GROUP)
                .build();

        Date startTime = DateBuilder.nextGivenSecondDate(null, 30);
        SimpleTrigger simpleTrigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                .withIdentity("trigger", Scheduler.DEFAULT_GROUP)
                .startAt(startTime)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(10)
                        .withRepeatCount(10))
                .forJob(jobDetail)
                .build();

        /* 交由调度器调度Job */
        try {
            scheduler.scheduleJob(jobDetail, simpleTrigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws Exception {
        StdSchedulerFactory factory = new StdSchedulerFactory();
        factory.initialize("quartz.properities");
        scheduler = factory.getScheduler();
    }

    @Override
    public void start() throws Exception {
        scheduler.start();
    }

    @Override
    public void stop() throws Exception {
        scheduler.shutdown();
    }
}
