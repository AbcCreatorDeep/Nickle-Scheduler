package nickle.scheduler.server.service.impl;

import nickle.scheduler.server.entity.NickleSchedulerLock;
import nickle.scheduler.server.mapper.NickleSchedulerLockMapper;
import nickle.scheduler.server.service.INickleSchedulerLockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author nickle
 * @since 2019-05-07
 */
@Service
public class NickleSchedulerLockServiceImpl extends ServiceImpl<NickleSchedulerLockMapper, NickleSchedulerLock> implements INickleSchedulerLockService {

}
