package nickle.scheduler.server.manager.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import nickle.scheduler.server.entity.NickleSchedulerExecutor;
import nickle.scheduler.server.entity.NickleSchedulerExecutorJob;
import nickle.scheduler.server.manager.controller.pojo.vo.ExecutorVO;
import nickle.scheduler.server.service.INickleSchedulerExecutorJobService;
import nickle.scheduler.server.service.INickleSchedulerExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("executor")
public class ExecutorController {
    @Autowired
    private INickleSchedulerExecutorService executorService;
    @Autowired
    private INickleSchedulerExecutorJobService executorJobService;

    @RequestMapping("/executorList")
    public List<ExecutorVO> executorList() {
        List<NickleSchedulerExecutor> list = executorService.list();
        List<ExecutorVO> collect = list.stream().parallel().map(executor -> {
            ExecutorVO executorVO = new ExecutorVO();
            executorVO.setExecutorId(executor.getExecutorId());
            executorVO.setExecutorName(executor.getExecutorName());
            QueryWrapper<NickleSchedulerExecutorJob> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(NickleSchedulerExecutorJob::getExecutorName, executor.getExecutorName());
            executorVO.setJobNum(executorJobService.count(queryWrapper));
            return executorVO;
        }).collect(Collectors.toList());
        return collect;
    }
}
