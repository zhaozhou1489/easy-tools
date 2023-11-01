package com.marmot.tools.db_query.tools;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.marmot.tools.db_query.enums.QueryOperatorEnum;
import com.marmot.tools.db_query.enums.QueryOrderEnum;
import com.marmot.tools.db_query.params.QueryCond;
import com.marmot.tools.db_query.params.QueryParam;
import com.marmot.tools.db_query.query.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author:zhaozhou
 * @Date: 2023/07/18
 * @Desc: 将QueryCond查询参数，转换为QueryWrapper
 */
public class QueryWrapperUtil {

    //构建查询
    public static QueryWrapper transQueryCond(QueryWrapper wrapper, QueryCond cond){
        transQueries(wrapper,cond.getQueries());
        setLimit(wrapper,cond.getLimit());
        setOrderBy(wrapper,cond.getOrders());
        return wrapper;
    }

    private static <T extends AbstractBaseQuery> QueryWrapper transWithBooleanQuery(QueryWrapper wrapper, List<T> queries, String parentOperator){
        for (int i = 0;i < queries.size(); i++){
            AbstractBaseQuery q = queries.get(i);
            if (q instanceof BooleanQuery){
                if (parentOperator.equals(QueryOperatorEnum.OR.getOperator())){
                    wrapper.or(wq -> transWithBooleanQuery((QueryWrapper)wq, ((BooleanQuery) q).getQueries(), ((BooleanQuery) q).getOperator()));
                }else {
                    wrapper.and(wq -> transWithBooleanQuery((QueryWrapper)wq, ((BooleanQuery) q).getQueries(), ((BooleanQuery) q).getOperator()));
                }
            }
            if (q instanceof AbstractFieldQuery){
                ((AbstractFieldQuery) q).setField(StrUtil.toUnderlineCase(((AbstractFieldQuery) q).getField()));
                if (i != 0){
                    if (parentOperator.equals(QueryOperatorEnum.OR.getOperator())){
                        wrapper.or();
                    }
                }
                transQuery(wrapper, q);
            }
        }
        return wrapper;
    }


    private static <T extends AbstractBaseQuery> QueryWrapper transQueries(QueryWrapper wrapper, List<T> queries){
        return transWithBooleanQuery(wrapper, queries, QueryOperatorEnum.AND.getOperator());
    }



    private static <T extends AbstractBaseQuery> AbstractWrapper transQuery(AbstractWrapper wrapper, T query){
        if (query instanceof EqualQuery){
            EqualQuery q = (EqualQuery) query;
            transEqualQuery(wrapper, q);
        }else if (query instanceof LikeQuery){
            LikeQuery q = (LikeQuery) query;
            transLikeQuery(wrapper,q);

        }else if (query instanceof RangeQuery){
            RangeQuery q = (RangeQuery) query;
            transRangeQuery(wrapper,q);

        }else if (query instanceof InQuery){
            InQuery q = (InQuery) query;
            transInQuery(wrapper,q);
        }else if (query instanceof NullQuery){
            NullQuery q = (NullQuery) query;
            transNullQuery(wrapper,q);
        }
        return wrapper;
    }



    private static AbstractWrapper transEqualQuery(AbstractWrapper wrapper, EqualQuery query){
        if (query.isOpposition()){
            wrapper.ne(query.getField(), query.getValue());
        }else {
            wrapper.eq(query.getField(), query.getValue());
        }
        return wrapper;
    }

    private static AbstractWrapper transLikeQuery(AbstractWrapper wrapper, LikeQuery query){
        if (query.isOpposition()){
            if (query.isLeft() && query.isRight()){
                wrapper.notLike(query.getField(), query.getLikeValue());
            }else if (query.isLeft()){
                wrapper.notLikeLeft(query.getField(), query.getLikeValue());
            }else {
                wrapper.notLikeRight(query.getField(), query.getLikeValue());
            }
        }else {
            if (query.isLeft() && query.isRight()){
                wrapper.like(query.getField(), query.getLikeValue());
            }else if (query.isLeft()){
                wrapper.likeLeft(query.getField(), query.getLikeValue());
            }else {
                wrapper.likeRight(query.getField(), query.getLikeValue());
            }
        }


        return wrapper;
    }

    private static AbstractWrapper transRangeQuery(AbstractWrapper wrapper, RangeQuery query){
        if (StringUtils.isNotBlank(query.getMax())){
            if (query.isIncludeMax()){
                wrapper.le(query.getField(), query.getMax());
            }else {
                wrapper.lt(query.getField(), query.getMax());
            }
        }
        if (StringUtils.isNotBlank(query.getMin())){
            if (query.isIncludeMin()){
                wrapper.ge(query.getField(), query.getMin());
            }else {
                wrapper.gt(query.getField(), query.getMin());
            }
        }

        return wrapper;
    }

    private static AbstractWrapper transInQuery(AbstractWrapper wrapper, InQuery query){
        if (query.isOpposition()){
            wrapper.notIn(query.getField(), query.getValues());
        }else {
            wrapper.in(query.getField(), query.getValues());
        }

        return wrapper;
    }

    private static AbstractWrapper transNullQuery(AbstractWrapper wrapper, NullQuery query){
        if (query.isOpposition()){
            wrapper.isNotNull(query.getField());
        }else {
            wrapper.isNull(query.getField());
        }

        return wrapper;
    }



    private static AbstractWrapper setLimit(AbstractWrapper wrapper, Limit limit){
        if (limit != null){
            wrapper.last("limit " + limit.getOffset() + " , " + limit.getCount());
        }
        return wrapper;
    }

    private static AbstractWrapper setOrderBy(AbstractWrapper wrapper, List<Order> orders){
        if (CollectionUtils.isEmpty(orders)){
            return wrapper;
        }
        //按order排序
        Collections.sort(orders, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return Integer.compare(o1.getOrder(), o2.getOrder());
            }
        });
        orders.stream().forEach(o -> {
            wrapper.orderBy(true,o.getSortStr().equalsIgnoreCase(QueryOrderEnum.ASC.getOrderStr()),StrUtil.toUnderlineCase(o.getField()));
        });

        return wrapper;
    }



    public static void main(String[] args){
        QueryParam queryParam = QueryBuilder.newBuilder()
                .addEqualQuery("test","121212")
                .addLikeQuery("name","tom")
                .addRangeQuery("createTime","1000","10000",true,false)
                .addInQuery("id", Arrays.asList("12","1212"))
                .setLimit(0,100)
                .addOrder("test",QueryOrderEnum.ASC)
                .addOrder("name",QueryOrderEnum.DESC)
                .addBooleanQuery(
                        QueryBuilder.newBuilder().addEqualQuery("test","1111111").addInQuery("name",Arrays.asList("1111","1111")),QueryOperatorEnum.OR)
                .addBooleanQuery(
                        QueryBuilder.newBuilder().addEqualQuery("test","222222").addInQuery("name",Arrays.asList("22222","2222")),QueryOperatorEnum.AND)
                .addBooleanQuery(
                        QueryBuilder.newBuilder().addEqualQuery("test","33333").addInQuery("name",Arrays.asList("3333","33333"))
                                .addBooleanQuery(QueryBuilder.newBuilder().addEqualQuery("createTime","444444").addInQuery("name",Arrays.asList("44444","44444")),QueryOperatorEnum.OR)
                                .addBooleanQuery(QueryBuilder.newBuilder().addEqualQuery("createTime","55555").addInQuery("name",Arrays.asList("55555","5555")),QueryOperatorEnum.AND)
                        ,QueryOperatorEnum.OR)
                .getQueryParam();
        System.out.println(JSONUtil.toJsonStr(queryParam));

        QueryCond cond = new QueryCond();
        String errMsg = QueryTransUtil.transQueryParam(queryParam,cond, Stream.of("test", "name","createTime","id").collect(Collectors.toSet()),Stream.of("test", "name","createTime","ddd").collect(Collectors.toSet()));
        System.out.println("trans errMsg=" + errMsg);

        QueryWrapper wrapper = new QueryWrapper();
        QueryWrapperUtil.transQueryCond(wrapper, cond);
        System.out.println("QueryWrapper=" + wrapper.getCustomSqlSegment());
    }



}
