package com.marmot.tools.db_query.tools;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.marmot.tools.db_query.enums.QueryTypeEnum;
import com.marmot.tools.db_query.query.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Author:zhaozhou
 * @Date: 2023/07/18
 * @Desc: 查询参数解析
 */
@Slf4j
public class QueryParser {
    private static final Map<String,Class<? extends AbstractBaseQuery>> queryNameClassMap = new HashMap<String,Class<? extends AbstractBaseQuery>>(){{
        put(QueryTypeEnum.EQUAL.getName(), EqualQuery.class);
        put(QueryTypeEnum.LIKE.getName(), LikeQuery.class);
        put(QueryTypeEnum.RANGE.getName(), RangeQuery.class);
        put(QueryTypeEnum.IN.getName(), InQuery.class);
        put(QueryTypeEnum.NULL.getName(), NullQuery.class);
        put(QueryTypeEnum.BOOLEAN.getName(), BooleanQuery.class);
    }};


    public static AbstractBaseQuery parseQuery(String queryStr, Class<? extends AbstractBaseQuery> queryClass){
        if (StringUtils.isBlank(queryStr)) return null;
        return JSONUtil.toBean(queryStr, queryClass);
    }


    public static String parseQueries(List<String> queryStrs, List<AbstractBaseQuery> queries) {
        if (CollectionUtils.isEmpty(queryStrs)) return "";
        for (String q : queryStrs) {
            //获取查询类型
            JSONObject jsonObj = JSONUtil.parseObj(q);
            String queryType = jsonObj.getStr("type");
            Class<? extends AbstractBaseQuery> queryClass = queryNameClassMap.get(queryType);
            if (queryClass == null) {
                return "type:" + queryType + " is not support";
            }
            //反序列化查询参数
            AbstractBaseQuery query = parseQuery(q, queryClass);
            queries.add(query);

            //若是Boolean查询，则解析子查询
            if (query instanceof BooleanQuery){
                List<AbstractBaseQuery> boolQueries = new LinkedList<>();
                //解析bool查询的子查询
                String errMsg = parseQueries(((BooleanQuery) query).getSubQueries(),boolQueries);
                if (StringUtils.isNotBlank(errMsg)){
                    log.error("parse boolean sub query error, subQuery={}, errMsg={}", ((BooleanQuery) query).getSubQueries(), errMsg);
                    return errMsg;
                }
                ((BooleanQuery) query).setQueries(boolQueries);
            }
        }
        return "";
    }



}
