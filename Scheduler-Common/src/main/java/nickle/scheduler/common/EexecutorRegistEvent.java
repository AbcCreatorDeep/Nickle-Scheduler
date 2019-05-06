package nickle.scheduler.common;

import lombok.Data;

/**
 * @author huangjun01
 * @title: EexecutorRegistEvent
 * @description:
 * @date 2019/5/6 18:05
 */
@Data
public class EexecutorRegistEvent {
    private String jobName;
    private String ip;
    private Integer port;
}
