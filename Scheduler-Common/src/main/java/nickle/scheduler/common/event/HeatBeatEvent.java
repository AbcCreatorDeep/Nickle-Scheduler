package nickle.scheduler.common.event;

import lombok.Data;

import java.io.Serializable;

/**
 * @author nickle
 * @description:
 * @date 2019/5/8 13:24
 */
@Data
public class HeatBeatEvent implements Serializable {

    private static final long serialVersionUID = -8501478724516543449L;
    private String ip;
    private Integer port;
}
