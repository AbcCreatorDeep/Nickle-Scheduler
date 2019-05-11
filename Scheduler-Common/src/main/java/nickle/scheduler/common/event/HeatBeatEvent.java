package nickle.scheduler.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author nickle
 * @description:
 * @date 2019/5/8 13:24
 */
@Data
@AllArgsConstructor
public final class HeatBeatEvent implements Serializable {
    private static final long serialVersionUID = -8501478724516543449L;
    private final String ip;
    private final Integer port;
    private final List<String> jobNameList;
}
