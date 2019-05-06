package nickle.scheduler.server.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import nickle.scheduler.common.EexecutorRegistEvent;
import nickle.scheduler.server.entity.NickleSchedulerExecutor;
import nickle.scheduler.server.mapper.NickleSchedulerExecutorMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.util.Date;

public class RegisterActor extends AbstractActor {
    private static SqlSessionFactory sqlSessionFactory;

    static {
        SqlSessionFactoryBuilder sqlSessionFactoryBuilder = new SqlSessionFactoryBuilder();
        sqlSessionFactory = sqlSessionFactoryBuilder.build(RegisterActor.class.getClassLoader().getResourceAsStream("mybatis-config.xml"));
    }

    public static Props props() {
        return Props.create(RegisterActor.class);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(EexecutorRegistEvent.class, this::execExecutorRegistEvent).build();
    }

    private void execExecutorRegistEvent(EexecutorRegistEvent executorRegistEvent) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            NickleSchedulerExecutorMapper nickleSchedulerExecutorMapper = sqlSession.getMapper(NickleSchedulerExecutorMapper.class);
            NickleSchedulerExecutor nickleSchedulerExecutor = new NickleSchedulerExecutor();
            nickleSchedulerExecutor.setCreateTime(new Date());
            nickleSchedulerExecutor.setJobName(executorRegistEvent.getJobName());
            nickleSchedulerExecutor.setPort(executorRegistEvent.getPort());
            nickleSchedulerExecutorMapper.insert(nickleSchedulerExecutor);
            sqlSession.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqlSession.close();
        }
    }
}
