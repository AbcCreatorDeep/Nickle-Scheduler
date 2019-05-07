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
public class NickleSchedulerLock implements Serializable {

private static final long serialVersionUID=1L;

    @TableId(value = "lock_id", type = IdType.AUTO)
    private Integer lockId;

    private String lockName;


}
