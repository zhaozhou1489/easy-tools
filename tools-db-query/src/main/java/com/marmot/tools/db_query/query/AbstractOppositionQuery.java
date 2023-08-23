package com.marmot.tools.db_query.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author:zhaozhou
 * @Date: 2023/07/18
 * @Desc: 支持反向查询的查询类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractOppositionQuery extends AbstractFieldQuery{
    /**
     * @Desc opposition:true：反向操作（如 not in, not like）；false：正向操作
     **/
    private boolean opposition;
}
