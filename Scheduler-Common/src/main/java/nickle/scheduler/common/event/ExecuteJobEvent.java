package nickle.scheduler.common.event;

import lombok.Data;

/**
 * @author nickle
 * @description:
 * @date 2019/5/7 18:02
 */
@Data
public class ExecuteJobEvent {
    private String className;
}
