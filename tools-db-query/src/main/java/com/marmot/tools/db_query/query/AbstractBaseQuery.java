package com.marmot.tools.db_query.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author:zhaozhou
 * @Date: 2023/07/18
 * @Desc: 基础查询类型，标明查询类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractBaseQuery {
    private String type;
}
