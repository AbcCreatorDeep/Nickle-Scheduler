package nickle.scheduler.server.mapper;

import java.util.List;
import nickle.scheduler.server.entity.NickleSchedulerExecutor;
import nickle.scheduler.server.entity.NickleSchedulerExecutorExample;
import org.apache.ibatis.annotations.Param;

public interface NickleSchedulerExecutorMapper {
    int countByExample(NickleSchedulerExecutorExample example);

    int deleteByExample(NickleSchedulerExecutorExample example);

    int insert(NickleSchedulerExecutor record);

    int insertSelective(NickleSchedulerExecutor record);

    List<NickleSchedulerExecutor> selectByExample(NickleSchedulerExecutorExample example);

    int updateByExampleSelective(@Param("record") NickleSchedulerExecutor record, @Param("example") NickleSchedulerExecutorExample example);

    int updateByExample(@Param("record") NickleSchedulerExecutor record, @Param("example") NickleSchedulerExecutorExample example);
}