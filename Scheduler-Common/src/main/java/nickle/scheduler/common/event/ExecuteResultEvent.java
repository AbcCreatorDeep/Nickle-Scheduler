package nickle.scheduler.common.event;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExecuteResultEvent implements Serializable {
    private static final long serialVersionUID = 5930026674578943550L;
    private Throwable throwable;
    private Integer runJobId;
}
