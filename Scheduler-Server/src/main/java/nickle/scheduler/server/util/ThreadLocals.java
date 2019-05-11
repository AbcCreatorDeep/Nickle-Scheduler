package nickle.scheduler.server.util;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import org.apache.ibatis.session.SqlSession;

/**
 * threadlocal类
 *
 * @author nickle
 */
public class ThreadLocals {
    public static final ThreadLocal<SqlSession> SQL_SESSION_THREAD_LOCAL = new ThreadLocal<>();
    public static final ThreadLocal<ActorContext> ACTOR_CONTEXT_THREAD_LOCAL = new ThreadLocal<>();
    public static final ThreadLocal<ActorRef> ACTOR_REF_THREAD_LOCAL = new ThreadLocal<>();


    public static ActorContext getActorContext() {
        return ACTOR_CONTEXT_THREAD_LOCAL.get();
    }

    public static ActorRef getActorRef() {
        return ACTOR_REF_THREAD_LOCAL.get();
    }

    public static void setActorRef(ActorRef actorRef) {
        ACTOR_REF_THREAD_LOCAL.set(actorRef);
    }

    public static void setActorContext(ActorContext actorContext) {
        ACTOR_CONTEXT_THREAD_LOCAL.set(actorContext);
    }

    public static void releaseActorRef() {
        ACTOR_REF_THREAD_LOCAL.remove();
    }

    public static void releaseActorContext() {
        ACTOR_CONTEXT_THREAD_LOCAL.remove();
    }

    public static void setSqlSession(SqlSession sqlSession) {
        SQL_SESSION_THREAD_LOCAL.set(sqlSession);
    }

    public static SqlSession getSqlSession() {
        return SQL_SESSION_THREAD_LOCAL.get();
    }

    public static void releaseSqlSession() {
        SQL_SESSION_THREAD_LOCAL.remove();
    }

    public static void releaseAll() {
        releaseActorRef();
        releaseActorContext();
        releaseSqlSession();
    }

}
