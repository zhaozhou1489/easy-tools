package com.marmot.tools.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author:zhaozhou
 * @Date: 2023/08/16
 * @Desc: 基本任务封装
 */
@Slf4j
public abstract class AbstractBaseTask {


    /**
     * @Desc
     *  使用：1、必须重写processTask和doTask；
     *       2、@Scheduled注解写在processTask上，且processTask不做实现，直接使用super的实现；@Scheduled尽量用cron表达式；
     *       3、doTask为任务的业务实现；
     **/

    @Autowired
    protected ScheduleNodeLockService nodeLockService;

    private volatile long startTs = 0;



    protected void processTask() {
        String taskName = this.getClass().getName();
        try {
            log.debug("##processTask:{}",taskName );
            if (nodeLockService.locked()) {
                this.doBeforeTask();
                this.doTask();
                this.doAfterTask();
            }
        } catch (Exception e) {
            this.doOnException(e);
        }
    }

    public abstract void doTask();


    public void doBeforeTask(){
        startTs = System.currentTimeMillis();
        log.info("###### start:{} #######", this.getClass().getName());
    };

    public void doAfterTask(){
        log.info("###### end:{} #######, MS={}",this.getClass().getName(), (System.currentTimeMillis() - startTs));
    };


    public void doOnException(Exception e){
        log.error("Task fail:" + this.getClass().getName(), e);
    }
}