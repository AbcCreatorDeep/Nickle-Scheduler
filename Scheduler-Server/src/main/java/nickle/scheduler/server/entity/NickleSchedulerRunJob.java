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
 * @since 2019-05-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class NickleSchedulerRunJob implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "run_job_id", type = IdType.AUTO)
    private Integer runJobId;

    private String jobName;

    private String triggerName;

    private Integer executorId;

    private Long scheduleTime;

    private Long updateTime;


}
