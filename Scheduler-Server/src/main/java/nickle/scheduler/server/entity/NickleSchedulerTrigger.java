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
public class NickleSchedulerTrigger implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "trigger_id", type = IdType.AUTO)
    private Integer triggerId;

    private String triggerName;

    private String triggerCron;

    private Long triggerNextTime;
    /**
     * 0：standby，1：acquired，2，suspend
     */
    private Integer triggerStatus;

    private Long triggerUpdateTime;


}
