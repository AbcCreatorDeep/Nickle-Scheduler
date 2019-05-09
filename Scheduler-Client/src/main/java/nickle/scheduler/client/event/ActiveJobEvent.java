package nickle.scheduler.client.event;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author nickle
 * @description:
 * @date 2019/5/9 12:00
 */
@Data
public class ActiveJobEvent implements Serializable {
    private static final long serialVersionUID = -1003883468325112030L;
    private List<String> jobNameList;
}
