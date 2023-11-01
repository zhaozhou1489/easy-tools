package com.marmot.tools.db_query.tools;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.marmot.tools.db_query.enums.QueryOperatorEnum;
import com.marmot.tools.db_query.enums.QueryOrderEnum;
import com.marmot.tools.db_query.params.QueryCond;
import com.marmot.tools.db_query.params.QueryParam;
import com.marmot.tools.db_query.query.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author:zhaozhou
 * @Date: 2023/08/01
 * @Desc: 查询参数解析及转换，将QueryParam转换为QueryCond
 */

@Slf4j
public class QueryTransUtil {


    private static final long LIMIT_COUNT_MAX = 10000;

    /**
     * @Desc 转换并校验查询参数
     * @Param
     * @return
     **/
    public static String transQueryParam(QueryParam queryParam, QueryCond queryCond, Set<String> validFields, Set<String> requiredFields) {
        String errMsg = "";
        List<AbstractBaseQuery> queries = new LinkedList<>();

        log.info("start parse, input:{}", JSONUtil.toJsonStr(queryParam));
        queryCond.setQueries(new LinkedList<>());
        if (queryParam == null){
            return "";
        }
        //解析及校验查询参数
        errMsg = StringUtils.isNotBlank(errMsg = QueryParser.parseQueries(queryParam.getQueries(), queries)) ? errMsg : QueryValidator.validQueries(queries);
        if (StringUtils.isNotBlank(errMsg)) {
            log.error("parse and verify queries fail, errMsg={}", errMsg);
            return errMsg;
        }

        if (queryParam.getLimit() != null && queryParam.getLimit().getCount() > LIMIT_COUNT_MAX) {
            log.error("limit count should less than " + LIMIT_COUNT_MAX);
            return "limit count should less than " + LIMIT_COUNT_MAX;
        }
        if (queryParam.getLimit() == null){
            queryParam.setLimit(new Limit(0L, LIMIT_COUNT_MAX));
        }

        if (CollectionUtils.isNotEmpty(queryParam.getOrders())){
            queryParam.getOrders().forEach(order -> order.setSortStr(QueryOrderEnum.of(order.getSortStr()).getOrderStr()));
        }
        queryCond.setQueries(queries);
        queryCond.setLimit(queryParam.getLimit());
        queryCond.setOrders(queryParam.getOrders());

        //校验字段是否支持
        errMsg = validFieldOfQueryCond(queryCond, validFields,requiredFields);
        if (StringUtils.isNotBlank(errMsg)){
            log.error("verify field fail, msg={}", errMsg);
            return errMsg;
        }

        log.info("parse query success, output:" + JSONUtil.toJsonStr(queryCond));
        return errMsg;
    }



    /**
     * @Desc 检测查询参数是否合法
     **/
    private static String validFieldOfQueryCond(QueryCond queryCond, Set<String> validFields, Set<String> requiredFields){
        if (CollectionUtils.isEmpty(validFields)){
            return "";
        }
        //查询字段检查
        Set<String> queryInvalidFields = CollectionUtils.isEmpty(queryCond.getQueries()) ? new HashSet<>() :
                validQueryField(queryCond.getQueries(), validFields);
        Set<String> requiredFieldResult = validRequiredField(queryCond.getQueries(), new HashSet<>(requiredFields));
        if (CollectionUtils.isNotEmpty(requiredFieldResult)){
            return "Fields " + JSON.toJSONString(requiredFieldResult) + "are required";
        }
        //排序字段检查
        Set<String> orderInvalidFields = CollectionUtils.isEmpty(queryCond.getOrders()) ? new HashSet<>():
                queryCond.getOrders().stream().map(Order::getField).filter(field -> !validFields.contains(field)).collect(Collectors.toSet());

        Set<String> invalidFields = Stream.of(queryInvalidFields,orderInvalidFields).flatMap(Set::stream).collect(Collectors.toSet());
        return CollectionUtils.isEmpty(invalidFields) ? "": "fields " + JSONUtil.toJsonStr(invalidFields) + " are not support";
    }


    /**
     * @Desc 检测查询字段是否合法
     **/
    private static Set<String> validRequiredField(List<? extends AbstractBaseQuery> queries, Set<String> requiredFields){
        for (AbstractBaseQuery query:queries){
            if (query instanceof AbstractFieldQuery){
                if (!requiredFields.contains(((AbstractFieldQuery) query).getField())){
                    requiredFields.remove(((AbstractFieldQuery) query).getField());
                }
            }
            if (query instanceof BooleanQuery){
                validQueryField(((BooleanQuery) query).getQueries(), requiredFields);
            }
        }
        return requiredFields;
    }


    /**
     * @Desc 检测查询字段是否合法
     **/
    private static Set<String> validQueryField(List<? extends AbstractBaseQuery> queries, Set<String> validFields){
        Set<String> invalidFields = new HashSet<>();
        for (AbstractBaseQuery query:queries){
            if (query instanceof AbstractFieldQuery){
                if (!validFields.contains(((AbstractFieldQuery) query).getField())){
                    invalidFields.add(((AbstractFieldQuery) query).getField());
                }
            }
            if (query instanceof BooleanQuery){
                Set<String> boolInvalidFields = validQueryField(((BooleanQuery) query).getQueries(), validFields);
                if (CollectionUtils.isNotEmpty(boolInvalidFields)){
                    invalidFields.addAll(boolInvalidFields);
                }
            }
        }
        return invalidFields;
    }



    public static void main(String[] args){
        QueryParam queryParam = QueryBuilder.newBuilder()
                .addEqualQuery("test","121212")
                .addLikeQuery("name","tom")
                .addRangeQuery("createTime","1000","10000",true,false)
                .addInQuery("id", Arrays.asList("12","1212"))
                .setLimit(0,100)
                .addOrder("test", QueryOrderEnum.ASC)
                .addOrder("name",QueryOrderEnum.DESC)
                .addBooleanQuery(QueryBuilder.newBuilder().addEqualQuery("test","121212").addInQuery("name",Arrays.asList("1212","12131")), QueryOperatorEnum.OR)
                .getQueryParam();
        System.out.println(JSONUtil.toJsonStr(queryParam));

        String queryStr = JSONUtil.toJsonStr(queryParam);
        QueryParam qp = JSON.parseObject(queryStr,QueryParam.class);
        QueryCond cond = new QueryCond();
        transQueryParam(qp,cond,new HashSet<>(Arrays.asList("test","name","createTime")),new HashSet<>(Arrays.asList("test","name","createTime")));
        System.out.println(JSON.toJSON(cond));

    }
}
