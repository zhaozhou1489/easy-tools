package com.marmot.tools.task.lock_service;

import com.marmot.tools.task.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.Assert;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

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
public abstract class AbstractScheduleLockService implements LockService, ApplicationListener<ApplicationReadyEvent> {
    //默认锁名称
    private static String DEFAULT_LOCK_NAME = "node:lock:name";
    //默认锁过期时间
    private static final long DEFAULT_LOCK_EXPIRE_S = 10L;
    //默认定时任务执行时间
    private static final long DEFAULT_TASK_INTERVAL_S = 2L;

    //锁的名称
    private String lockName;

    //锁的过期时间
    private long lockExpireSeconds;

    //获取锁的定时任务的执行间隔
    private long taskIntervalSeconds;

    //true：当前进程获取到锁；false：当前进程未获取到锁
    private volatile boolean locked = false;
    //获取锁的定时任务调度线程
    private ScheduledThreadPoolExecutor LOCK_EXECUTOR = new ScheduledThreadPoolExecutor(1,new CustomizableThreadFactory("schedule-node-lock"));



    public long getTaskIntervalSeconds() {
        return taskIntervalSeconds;
    }

    public void setTaskIntervalSeconds(long taskIntervalSeconds) {
        Assert.isTrue(taskIntervalSeconds >= 0 , "taskIntervalSeconds need greater than 0");
        this.taskIntervalSeconds = taskIntervalSeconds;
    }


    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        Assert.hasText(lockName, "lockName is blank");
        this.lockName = lockName;
    }

    public long getLockExpireSeconds() {
        return lockExpireSeconds;
    }

    public void setLockExpireSeconds(long lockExpireSeconds) {
        Assert.isTrue(lockExpireSeconds >= 0 , "lockExpireSeconds need greater than 0");
        this.lockExpireSeconds = lockExpireSeconds;
    }

    public AbstractScheduleLockService() {
        this.lockName = DEFAULT_LOCK_NAME;
        this.lockExpireSeconds = DEFAULT_LOCK_EXPIRE_S;
        this.taskIntervalSeconds = DEFAULT_TASK_INTERVAL_S;
    }

    public AbstractScheduleLockService(String lockName, long lockExpireSeconds, long taskIntervalSeconds) {
        Assert.hasText(lockName, "lockName is blank");
        Assert.isTrue(lockExpireSeconds >= 0 , "lockExpireSeconds need greater than 0");
        Assert.isTrue(taskIntervalSeconds >= 0 , "taskIntervalSeconds need greater than 0");
        this.lockName = lockName;
        this.lockExpireSeconds = lockExpireSeconds;
        this.taskIntervalSeconds = taskIntervalSeconds;
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        //尝试获取锁
        this.tryLock();
        //定时任务，每TASK_INTERVAL_S秒钟尝试获取一次锁
        LOCK_EXECUTOR.scheduleAtFixedRate(()-> this.tryLock(), taskIntervalSeconds,taskIntervalSeconds, TimeUnit.SECONDS);
        //jvm钩子，关闭时主动释放锁
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("#####process task shutdown hook");
            String value = this.getValue();
            String localIp = IpUtils.getHostIp();
            log.info("#####lock info, ip={},value={}", localIp,value);
            if (locked && localIp.equals(value)){
                log.info("#####release lock");
                this.setExpire(0);
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
                log.debug("#####update lock expire seconds");
                this.setExpire(lockExpireSeconds);
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
            log.error("####AbstractScheduleLockService.tryLock fail", e);
        }

    }

    protected abstract String getValue();



    protected abstract void setExpire(long expireSeconds);


    protected abstract boolean setIfAbsent(String value);


    public boolean locked(){
        return locked;
    }
}
