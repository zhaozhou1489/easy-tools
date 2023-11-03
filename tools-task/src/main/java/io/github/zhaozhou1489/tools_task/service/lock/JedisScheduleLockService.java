package io.github.zhaozhou1489.tools_task.service.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;


/**
 * @Author:zhaozhou
 * @Date: 2023/08/15
 * @Desc: 基于jedis的节点锁处理
 */
@Slf4j
public abstract class JedisScheduleLockService extends AbstractScheduleLockService {

    private Jedis jedis;


    public JedisScheduleLockService(Jedis jedis) {
        Assert.notNull(jedis, "jedis is null");
        this.jedis = jedis;
    }

    public JedisScheduleLockService(Jedis jedis, String lockName, long lockExpireSeconds, long taskIntervalSeconds) {
        super(lockName, lockExpireSeconds, taskIntervalSeconds);
        Assert.notNull(jedis, "jedis is null");
        this.jedis = jedis;
    }

    @Override
    protected String getValue() {
        return jedis.get(this.getLockName());
    }

    @Override
    protected void setExpire(long expireSeconds) {
        jedis.expire(this.getLockName(), expireSeconds);
    }

    @Override
    protected boolean setIfAbsent(String value) {
        String result = jedis.set(this.getLockName(), value, SetParams.setParams().nx().ex(this.getLockExpireSeconds()));
        return "OK".equalsIgnoreCase(result);
    }
}
