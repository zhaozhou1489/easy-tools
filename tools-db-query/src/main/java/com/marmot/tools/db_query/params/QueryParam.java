package com.marmot.tools.db_query.params;

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
public class QueryParam {
    //查询子句，如equal，like等
    private List<String> queries;

    //limit
    private Limit limit;

    //排序方式
    private List<Order> orders;
}
