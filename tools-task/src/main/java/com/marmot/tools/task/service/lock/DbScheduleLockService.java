package com.marmot.tools.task.service.lock;

import com.marmot.tools.task.entity.TaskLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @Author:zhaozhou
 * @Date: 2023/08/15
 * @Desc: 基于数据库的节点锁处理
 */

@Slf4j
public abstract class DbScheduleLockService extends AbstractScheduleLockService {

    private JdbcTemplate jdbcTemplate;

    public DbScheduleLockService(DataSource dataSource) {
        Assert.notNull(dataSource, "dataSource is null");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public DbScheduleLockService(DataSource dataSource, String lockName, long lockExpireSeconds, long taskIntervalSeconds) {
        super(lockName, lockExpireSeconds, taskIntervalSeconds);
        Assert.notNull(dataSource, "dataSource is null");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }



    protected String getValue(){
        TaskLock taskLock = jdbcTemplate.queryForObject("select * from task_lock where lock_name=" + this.getLockName(), TaskLock.class);
        return taskLock == null || taskLock.getExpireAt() < System.currentTimeMillis() ? "": taskLock.getLockValue();
    }



    protected void setExpire(long expireSeconds){
        jdbcTemplate.update("update task_lock set expire_at= expire_at + " + expireSeconds);
    }


    protected boolean setIfAbsent(String value){
        Connection conn = null;
        boolean ac = false;
        try {
            conn = jdbcTemplate.getDataSource().getConnection();
            ac = conn.getAutoCommit();
            conn.setAutoCommit(false);
            TaskLock taskLock = jdbcTemplate.queryForObject("select * from task_lock where lock_name=" + this.getLockName() + " for update", TaskLock.class);

            int ret = 0;
            long expireAt = System.currentTimeMillis() + this.getLockExpireSeconds() * 1000;
            if (taskLock == null || taskLock.getExpireAt() < System.currentTimeMillis()){
                taskLock = new TaskLock(null,this.getLockName(),value,expireAt,System.currentTimeMillis());
                String sql = "INSERT INTO  task_lock (`lock_name`, `lock_value`, `expire_at`,'modify_stamp') VALUES (?, ?, ?, ?);";
                ret = jdbcTemplate.update(sql, taskLock.getLockName(), taskLock.getLockValue(), taskLock.getExpireAt(), taskLock.getModifyStamp());
            }else {
                String sql = "update  task_lock set 'lock_value=?', 'expire_at=?','modify_stamp=?') where lock_name=?;";
                ret = jdbcTemplate.update(sql,value,expireAt,System.currentTimeMillis(), this.getLockName());
            }
            conn.commit();
            return ret > 0;
        }catch (Exception e){
            log.error("Error occured, cause by: ", e);
            try {
                if (conn != null){
                    conn.rollback();
                }
            }catch (Exception ex){
                log.error("Error occurred while rollback, cause by:", ex);
            }

        }finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(ac);
                    conn.close();
                } catch (SQLException e) {
                    log.error("Error occurred while closing connectin, cause by:", e);
                }
            }
        }
        return false;
    }
}
