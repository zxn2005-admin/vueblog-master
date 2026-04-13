package com.markerhub.config;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisSessionDAO extends AbstractSessionDAO {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String prefix = "shiro:session:";
    private final long expireTime = 1800;

    public RedisSessionDAO(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session, sessionId);
        saveSession(session);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        if (sessionId == null) {
            return null;
        }
        return (Session) redisTemplate.opsForValue().get(prefix + sessionId.toString());
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        saveSession(session);
    }

    @Override
    public void delete(Session session) {
        if (session == null || session.getId() == null) {
            return;
        }
        redisTemplate.delete(prefix + session.getId().toString());
    }

    @Override
    public Collection<Session> getActiveSessions() {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        Set<Session> sessions = new HashSet<>();
        if (keys != null) {
            for (String key : keys) {
                Session session = (Session) redisTemplate.opsForValue().get(key);
                if (session != null) {
                    sessions.add(session);
                }
            }
        }
        return sessions;
    }

    private void saveSession(Session session) {
        if (session == null || session.getId() == null) {
            return;
        }
        redisTemplate.opsForValue().set(prefix + session.getId().toString(), session, expireTime, TimeUnit.SECONDS);
    }
}
