package nickle.scheduler.server.manager.controller.pojo.vo;

import lombok.Data;
import nickle.scheduler.server.entity.NickleSchedulerExecutor;

@Data
public class ExecutorVO extends NickleSchedulerExecutor {
    private int jobNum;
}
