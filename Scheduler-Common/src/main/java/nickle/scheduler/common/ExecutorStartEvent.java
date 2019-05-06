package nickle.scheduler.common;

import lombok.Data;

/**
 * @author huangjun01
 * @title: ExecutorStartEvent
 * @description:
 * @date 2019/5/6 14:47
 */
@Data
public class ExecutorStartEvent {
    private String className;
    private Integer spliceNum;
}
