package com.markerhub.config;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RedisCacheManager implements CacheManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<>();
    private long expireTime = 1800;

    public RedisCacheManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public <K, V> Cache<K, V> getCache(String name) {
        Cache<K, V> cache = caches.get(name);
        if (cache == null) {
            cache = new RedisCache<>(redisTemplate, name + ":", expireTime);
            caches.put(name, cache);
        }
        return cache;
    }
}
