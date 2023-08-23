package com.marmot.tools.db_query.params;

import com.marmot.tools.db_query.query.AbstractBaseQuery;
import com.marmot.tools.db_query.query.Limit;
import com.marmot.tools.db_query.query.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author:zhaozhou
 * @Date: 2023/07/24
 * @Desc:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryCond {
    private List<? extends AbstractBaseQuery> queries;
    private Limit limit;
    private List<Order> orders;
}
