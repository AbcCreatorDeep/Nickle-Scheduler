package nickle.scheduler.server.service.impl;

import nickle.scheduler.server.entity.NickleSchedulerJob;
import nickle.scheduler.server.mapper.NickleSchedulerJobMapper;
import nickle.scheduler.server.service.INickleSchedulerJobService;
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
public class NickleSchedulerJobServiceImpl extends ServiceImpl<NickleSchedulerJobMapper, NickleSchedulerJob> implements INickleSchedulerJobService {

}
