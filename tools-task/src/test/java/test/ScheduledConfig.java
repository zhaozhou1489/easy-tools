package test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author:zhaozhou
 * @Date: 2023/08/15
 * @Desc: 定时任务配置
 */
@Slf4j
public class ScheduledConfig implements SchedulingConfigurer {


    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 4;

    private static final int MAX_POOL_SIZE = Math.max(CORE_POOL_SIZE,100);

    private static final int QUEUE_CAPACITY = 4096;

    private static final int POOL_KEEP_ALIVE_S = 60 * 60;




    /**
     * @Desc 设置任务调度线程
     **/
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE, new CustomizableThreadFactory("task-schedule-"), new ThreadPoolExecutor.CallerRunsPolicy());
        threadPool.setKeepAliveTime(POOL_KEEP_ALIVE_S, TimeUnit.SECONDS);
        threadPool.allowCoreThreadTimeOut(true);
        taskRegistrar.setScheduler(threadPool);
    }


    /**
     * @Desc 设置异步任务执行线程
     **/
    @Bean("taskExecutor")
    public Executor  taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //设置核心线程数
        executor.setCorePoolSize(CORE_POOL_SIZE);
        //设置最大线程数
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        //设置任务队列容量
        executor.setQueueCapacity(QUEUE_CAPACITY);
        //设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(POOL_KEEP_ALIVE_S);
        //设置默认线程名称(线程前缀名称，有助于区分不同线程池之间的线程)
        executor.setThreadNamePrefix("task-executor-");
        //设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //设置允许核心线程超时，默认是false
        executor.setAllowCoreThreadTimeOut(true);

        executor.initialize();
        return executor;
    }

}
