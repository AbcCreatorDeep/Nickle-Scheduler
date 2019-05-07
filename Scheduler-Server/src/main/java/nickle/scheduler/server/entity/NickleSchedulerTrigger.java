package nickle.scheduler.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author nickle
 * @since 2019-05-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class NickleSchedulerTrigger implements Serializable {

private static final long serialVersionUID=1L;

    @TableId(value = "trigger_id", type = IdType.AUTO)
    private Integer triggerId;

    private String triggerName;

    private String triggerCron;

    private Long triggerNextTime;


}
