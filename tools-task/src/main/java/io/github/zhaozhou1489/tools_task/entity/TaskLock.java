package io.github.zhaozhou1489.tools_task.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author:zhaozhou
 * @Date: 2023/08/30
 * @Desc:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskLock {
    private Long id;

    private String lockName;

    private String lockValue;

    private Long expireAt;

    private Long modifyStamp;
}
