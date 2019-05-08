package nickle.scheduler.server.mapper;

import nickle.scheduler.server.entity.NickleSchedulerExecutor;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author nickle
 * @since 2019-05-08
 */
public interface NickleSchedulerExecutorMapper extends BaseMapper<NickleSchedulerExecutor> {
    /**
     * 根据端口和iP更新心跳时间戳
     *
     * @param ip
     * @param port
     * @param time
     * @return
     */
    int updateByIpAndPort(@Param("ip") String ip, @Param("port") int port, @Param("time") Long time);
}
