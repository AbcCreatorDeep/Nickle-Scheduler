package nickle.scheduler.client.event;

import lombok.Data;
import nickle.scheduler.common.event.RegisterEvent;

import java.util.List;

/**
 * 客户端注册事件
 */
@Data
public class ClientRegisterEvent {
    List<RegisterEvent.JobData> jobDataList;
    List<RegisterEvent.TriggerData> triggerDataList;
}
