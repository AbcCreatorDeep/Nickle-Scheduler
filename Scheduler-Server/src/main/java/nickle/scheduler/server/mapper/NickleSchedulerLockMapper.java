package nickle.scheduler.server.mapper;

import nickle.scheduler.server.entity.NickleSchedulerLock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author nickle
 * @since 2019-05-08
 */
public interface NickleSchedulerLockMapper extends BaseMapper<NickleSchedulerLock> {
    void lock(String lockName);
}
