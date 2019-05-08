package nickle.scheduler.server.service.impl;

import nickle.scheduler.server.entity.NickleSchedulerExecutorJob;
import nickle.scheduler.server.mapper.NickleSchedulerExecutorJobMapper;
import nickle.scheduler.server.service.INickleSchedulerExecutorJobService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author nickle
 * @since 2019-05-08
 */
@Service
public class NickleSchedulerExecutorJobServiceImpl extends ServiceImpl<NickleSchedulerExecutorJobMapper, NickleSchedulerExecutorJob> implements INickleSchedulerExecutorJobService {

}
