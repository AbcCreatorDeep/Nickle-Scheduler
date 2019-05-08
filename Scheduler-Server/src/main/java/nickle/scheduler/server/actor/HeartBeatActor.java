package nickle.scheduler.server.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import lombok.extern.slf4j.Slf4j;
import nickle.scheduler.common.event.HeatBeatEvent;
import nickle.scheduler.server.mapper.NickleSchedulerExecutorMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * @author nickle
 * @description:
 * @date 2019/5/8 13:20
 */
@Slf4j
public class HeartBeatActor extends AbstractActor {
    private SqlSessionFactory sqlSessionFactory;

    public HeartBeatActor(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public void preStart() throws Exception {
        log.info("心跳检测器启动");
    }

    public static Props props(SqlSessionFactory sqlSessionFactory) {
        return Props.create(HeartBeatActor.class, () -> new HeartBeatActor(sqlSessionFactory));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(HeatBeatEvent.class, this::heartBeat).build();
    }

    private void heartBeat(HeatBeatEvent heatBeatEvent) {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        log.info("检测到心跳开始:{}", heatBeatEvent);
        try {
            updateExecutorTime(sqlSession, heatBeatEvent);
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            log.error("心跳发生错误:{}", e.getMessage());
        } finally {
            sqlSession.close();
        }
        log.info("检测到心跳结束:{}", heatBeatEvent);
    }

    /**
     * 更新执行器时间
     *
     * @param sqlSession
     * @param heatBeatEvent
     */
    private void updateExecutorTime(SqlSession sqlSession, HeatBeatEvent heatBeatEvent) {
        //更新主机
        NickleSchedulerExecutorMapper executorMapper = sqlSession.getMapper(NickleSchedulerExecutorMapper.class);
        log.info("更新主机updateTime:{}", heatBeatEvent);
        executorMapper.updateByIpAndPort(heatBeatEvent.getIp(), heatBeatEvent.getPort(), System.currentTimeMillis());
    }

}
