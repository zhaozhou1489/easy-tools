package com.marmot.tools.task.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author:zhaozhou
 * @Date: 2023/10/20
 * @Desc:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRecord {
    private Long id;

    private String taskName;

    private Integer status;

    private Long startTime;

    private Long endTime;
}
