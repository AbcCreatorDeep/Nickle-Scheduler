package nickle.scheduler.server.mapper;

import java.util.List;
import nickle.scheduler.server.entity.NickleSchedulerJob;
import nickle.scheduler.server.entity.NickleSchedulerJobExample;
import org.apache.ibatis.annotations.Param;

public interface NickleSchedulerJobMapper {
    int countByExample(NickleSchedulerJobExample example);

    int deleteByExample(NickleSchedulerJobExample example);

    int insert(NickleSchedulerJob record);

    int insertSelective(NickleSchedulerJob record);

    List<NickleSchedulerJob> selectByExample(NickleSchedulerJobExample example);

    int updateByExampleSelective(@Param("record") NickleSchedulerJob record, @Param("example") NickleSchedulerJobExample example);

    int updateByExample(@Param("record") NickleSchedulerJob record, @Param("example") NickleSchedulerJobExample example);
}