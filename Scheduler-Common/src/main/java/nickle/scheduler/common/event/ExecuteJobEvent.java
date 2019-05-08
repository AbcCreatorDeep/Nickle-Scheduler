package nickle.scheduler.common.event;

import lombok.Data;

import java.io.Serializable;

/**
 * @author nickle
 * @description:
 * @date 2019/5/7 18:02
 */
@Data
public class ExecuteJobEvent implements Serializable {
    private static final long serialVersionUID = 110258378672068099L;
    private String className;
}
