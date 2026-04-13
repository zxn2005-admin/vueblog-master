package com.markerhub.config;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisCache<K, V> implements Cache<K, V> {

    private final RedisTemplate<String, Object> redisTemplate;
    private final String prefix;
    private final long expireTime;

    public RedisCache(RedisTemplate<String, Object> redisTemplate, String prefix, long expireTime) {
        this.redisTemplate = redisTemplate;
        this.prefix = prefix;
        this.expireTime = expireTime;
    }

    @Override
    public V get(K key) throws CacheException {
        if (key == null) {
            return null;
        }
        Object value = redisTemplate.opsForValue().get(prefix + key.toString());
        return (V) value;
    }

    @Override
    public V put(K key, V value) throws CacheException {
        if (key == null) {
            return null;
        }
        redisTemplate.opsForValue().set(prefix + key.toString(), value, expireTime, TimeUnit.SECONDS);
        return value;
    }

    @Override
    public V remove(K key) throws CacheException {
        if (key == null) {
            return null;
        }
        V value = get(key);
        redisTemplate.delete(prefix + key.toString());
        return value;
    }

    @Override
    public void clear() throws CacheException {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public int size() {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        return keys != null ? keys.size() : 0;
    }

    @Override
    public Set<K> keys() {
        return (Set<K>) redisTemplate.keys(prefix + "*");
    }

    @Override
    public Collection<V> values() {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        return (Collection<V>) redisTemplate.opsForValue().multiGet(keys);
    }
}
