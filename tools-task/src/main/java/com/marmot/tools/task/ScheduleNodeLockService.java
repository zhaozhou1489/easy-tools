package com.marmot.tools.task;

import com.marmot.tools.task.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import javax.sql.DataSource;
import java.time.Duration;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author:zhaozhou
 * @Date: 2023/08/15
 * @Desc: 定时任务节点锁处理
 */

/**
 * 主体逻辑：
 * 1、定时任务每2s执行尝试获取锁等操作。若当前进程已经获取锁，则重新设置锁过期时间（10s）；若当前进行未获取到锁，则尝试获取锁；
 * 2、注册jvm钩子，当jvm关闭时，若当前进程已经获取到锁，则主动释放锁；
 * 3、AbstractBaseTask.processTask中，若当前进程已经获取到锁，则可以执行任务，否则直接跳过任务执行；
 *
 *
 **/
@Slf4j
public abstract class ScheduleNodeLockService implements ApplicationListener<ApplicationReadyEvent> {

    private RedissonClient redissonClient;

    private Jedis jedis;


    private static volatile boolean locked = false;
    private static String NODE_LOCK_NAME = "node:lock:user-account";
    private static final long LOCK_EXPIRE_S = 10L;
    private static final long TASK_INTERVAL_S = 2L;


    public ScheduleNodeLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public ScheduleNodeLockService(Jedis jedis) {
        this.jedis = jedis;
    }



    private static ScheduledThreadPoolExecutor LOCK_EXECUTOR = new ScheduledThreadPoolExecutor(1,new CustomizableThreadFactory("schedule-node-lock"));


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        //尝试获取锁
        this.tryLock();
        //定时任务，每TASK_INTERVAL_S秒钟尝试获取一次锁
        LOCK_EXECUTOR.scheduleAtFixedRate(()-> this.tryLock(), TASK_INTERVAL_S,TASK_INTERVAL_S, TimeUnit.SECONDS);
        //jvm钩子，关闭时主动释放锁
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("#####process task shutdown hook");
            RBucket<String> rBucket = redissonClient.getBucket(NODE_LOCK_NAME);
            String value = rBucket.get();
            String localIp = IpUtils.getHostIp();
            log.info("lock info, ip={},value={}", localIp,value);
            if (locked && localIp.equals(value)){
                log.info("release lock");
                rBucket.expire(Duration.ofSeconds(1));
            }
        }));
    }


    /**
     * @Desc 尝试获取锁
     **/
    private synchronized void tryLock(){
        try {
            String localIp = IpUtils.getHostIp();
            String value = this.getValue();


            //如果此进程已经获取到锁，则重新设置过期时间
            if (locked && localIp.equals(value)){
                this.setExpire();
                return;
            }
            //此进程尝试获取锁
            locked = false;
            boolean ret = this.setIfAbsent(localIp);
            if (ret){
                log.info("####### get node lock success ######");
                locked = true;
            }
        }catch (Exception e){
            log.error("####ScheduleNodeLockService.tryLock fail", e);
        }

    }

    private String getValue() {
        if (redissonClient != null) {
            RBucket<String> rBucket = redissonClient.getBucket(NODE_LOCK_NAME);
            return rBucket.get();
        } else {
            return jedis.get(NODE_LOCK_NAME);
        }
    }

    private void setExpire(){
        if (redissonClient != null) {
            RBucket<String> rBucket = redissonClient.getBucket(NODE_LOCK_NAME);
            rBucket.expire(Duration.ofSeconds(LOCK_EXPIRE_S));
        } else {
            jedis.expire(NODE_LOCK_NAME, LOCK_EXPIRE_S);
        }
    }

    private boolean setIfAbsent(String value){
        if (redissonClient != null) {
            RBucket<String> rBucket = redissonClient.getBucket(NODE_LOCK_NAME);
            return rBucket.setIfAbsent(IpUtils.getHostIp(), Duration.ofSeconds(LOCK_EXPIRE_S));
        } else {
            String result = jedis.set(NODE_LOCK_NAME, value, SetParams.setParams().nx().ex(LOCK_EXPIRE_S));
            return "OK".equalsIgnoreCase(result);
        }
    }


    public boolean locked(){
        return locked;
    }
}
