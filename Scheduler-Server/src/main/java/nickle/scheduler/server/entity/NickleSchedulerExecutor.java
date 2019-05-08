package nickle.scheduler.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author nickle
 * @since 2019-05-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class NickleSchedulerExecutor implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "executor_id", type = IdType.AUTO)
    private Integer executorId;

    private String executorIp;

    private Integer executorPort;

    private Long updateTime;


}
