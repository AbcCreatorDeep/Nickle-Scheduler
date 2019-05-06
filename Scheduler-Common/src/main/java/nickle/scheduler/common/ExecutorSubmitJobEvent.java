package nickle.scheduler.common;

import lombok.Data;


/**
 * @author huangjun01
 * @title: ExecutorRegistEvent
 * @description:
 * @date 2019/5/6 15:55
 */
@Data
public class ExecutorSubmitJobEvent {
    private String jobGroup;
    private String jobName;
    private String className;
    private String cronString;
    private String author;
    private String jobType;
}
