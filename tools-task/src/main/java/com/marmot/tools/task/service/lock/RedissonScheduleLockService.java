package com.marmot.tools.task.service.lock;

import com.marmot.tools.task.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.util.Assert;

import java.time.Duration;

/**
 * @Author:zhaozhou
 * @Date: 2023/08/15
 * @Desc: 基于redisson的节点锁处理
 */
@Slf4j
public abstract class RedissonScheduleLockService extends AbstractScheduleLockService {

    private RedissonClient redissonClient;


    public RedissonScheduleLockService(RedissonClient redissonClient) {
        Assert.notNull(redissonClient, "redissonClient is null");
        this.redissonClient = redissonClient;
    }

    public RedissonScheduleLockService(RedissonClient redissonClient, String lockName, long lockExpireSeconds, long taskIntervalSeconds) {
        super(lockName, lockExpireSeconds, taskIntervalSeconds);
        Assert.notNull(redissonClient, "redissonClient is null");
        this.redissonClient = redissonClient;
    }

    @Override
    protected String getValue() {
        RBucket<String> rBucket = redissonClient.getBucket(this.getLockName());
        return rBucket.get();
    }

    @Override
    protected void setExpire(long expireSeconds) {
        RBucket<String> rBucket = redissonClient.getBucket(this.getLockName());
        rBucket.expire(Duration.ofSeconds(expireSeconds));
    }

    @Override
    protected boolean setIfAbsent(String value) {
        RBucket<String> rBucket = redissonClient.getBucket(this.getLockName());
        return rBucket.setIfAbsent(IpUtils.getHostIp(), Duration.ofSeconds(this.getLockExpireSeconds()));
    }
}
