package io.github.zhaozhou1489.tools_db_query.tools;

import cn.hutool.json.JSONUtil;
import io.github.zhaozhou1489.tools_db_query.enums.QueryOperatorEnum;
import io.github.zhaozhou1489.tools_db_query.enums.QueryOrderEnum;
import io.github.zhaozhou1489.tools_db_query.params.QueryParam;
import com.marmot.tools.db_query.query.*;
import io.github.zhaozhou1489.query.*;
import io.github.zhaozhou1489.tools_db_query.query.*;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @Author:zhaozhou
 * @Date: 2023/07/18
 * @Desc: 查询语句构建器
 */
public class QueryBuilder {
    //查询参数
    private QueryParam queryParam;

    //排序顺序缓存
    private int orderNum = 0;

    public static QueryBuilder newBuilder(){
        QueryBuilder qb = new QueryBuilder();
        qb.queryParam = new QueryParam();
        qb.getQueryParam().setQueries(new LinkedList<>());

        return qb;
    }


    /**
     * @Desc 添加Equal查询
     **/
    public QueryBuilder addEqualQuery(String field, String value){
        Assert.hasText(field,"field is blank");
        Assert.hasText(field,"value is blank");
        EqualQuery query = new EqualQuery(field,value,false);
        this.addQuery(query);
        return this;
    }

    /**
     * @Desc 添加NotEqual查询
     **/
    public QueryBuilder addNotEqualQuery(String field, String value){
        Assert.hasText(field,"field is blank");
        Assert.hasText(field,"value is blank");
        EqualQuery query = new EqualQuery(field,value,true);
        this.addQuery(query);
        return this;
    }

    /**
     * @Desc 添加Range查询
     **/
    public QueryBuilder addRangeQuery(String field, String max,String min,boolean includeMax, boolean includeMin){
        Assert.hasText(field,"field is blank");
        Assert.isTrue(StringUtils.hasText(max) || StringUtils.hasText(min), "max or min, at least one exist");
        RangeQuery query = new RangeQuery(field, max, min,includeMax,includeMin);
        this.addQuery(query);
        return this;
    }


    /**
     * @Desc 添加In查询
     **/
    public QueryBuilder addInQuery(String field, List<String> values){
        Assert.hasText(field,"field is blank");
        Assert.notEmpty(values, "valueSet should not empty");
        InQuery query = new InQuery(field,values,false);
        this.addQuery(query);
        return this;
    }

    /**
     * @Desc 添加NotIn查询
     **/
    public QueryBuilder addNotInQuery(String field, List<String> values){
        Assert.hasText(field,"field is blank");
        Assert.notEmpty(values, "valueSet should not empty");
        InQuery query = new InQuery(field,values,true);
        this.addQuery(query);
        return this;
    }

    private QueryBuilder addLikeQuery(String field, String likeValue, boolean left, boolean right,boolean opposition){
        Assert.hasText(field,"field is blank");
        Assert.hasText(likeValue, "likeValue should not blank");
        LikeQuery query = new LikeQuery(field,likeValue, left,right,opposition);
        this.addQuery(query);
        return this;
    }

    private void addQuery(AbstractBaseQuery query){
        this.queryParam.getQueries().add(JSONUtil.toJsonStr(query));
    }


    /**
     * @Desc 添加Like查询
     **/
    public QueryBuilder addLikeQuery(String field, String likeValue){
        return this.addLikeQuery(field, likeValue,true, true, false);
    }

    /**
     * @Desc 添加NotLike查询
     **/
    public QueryBuilder addNotLikeQuery(String field, String likeValue){
        return this.addLikeQuery(field, likeValue,true, true, true);
    }

    /**
     * @Desc 添加leftLike查询
     **/
    public QueryBuilder addLeftLikeQuery(String field, String likeValue){
        return this.addLikeQuery(field, likeValue,true, false,false);
    }

    /**
     * @Desc 添加NotleftLike查询
     **/
    public QueryBuilder addNoteLeftLikeQuery(String field, String likeValue){
        return this.addLikeQuery(field, likeValue,true, false,true);
    }

    /**
     * @Desc 添加RightLike查询
     **/
    public QueryBuilder addRightLikeQuery(String field, String likeValue){
        return this.addLikeQuery(field, likeValue,false, true,false);
    }

    /**
     * @Desc 添加rightLike查询
     **/
    public QueryBuilder addNotRightLikeQuery(String field, String likeValue){
        return this.addLikeQuery(field, likeValue,false, true,true);
    }

    /**
     * @Desc 添加isNull查询
     **/
    public QueryBuilder addNullQuery(String field){
        Assert.hasText(field,"field is blank");
        NullQuery query = new NullQuery(field,false);
        this.addQuery(query);
        return this;
    }

    /**
     * @Desc 添加isNotNull查询
     **/
    public QueryBuilder addNotNullQuery(String field){
        Assert.hasText(field,"field is blank");
        NullQuery query = new NullQuery(field,true);
        this.addQuery(query);
        return this;
    }

    /**
     * @Desc 添加Boolean查询
     **/
    public QueryBuilder addBooleanQuery(QueryBuilder qb, QueryOperatorEnum operator){
        Assert.notNull(operator,"operator is empty");
        Assert.notNull(qb,"qb is empty");
        Assert.notNull(qb.getQueryParam(),"qb.queryParam is empty");
        Assert.notEmpty(qb.getQueryParam().getQueries(),"qb.queryParam.queries is empty");
        this.addQuery(new BooleanQuery(qb.getQueryParam().getQueries(),operator.getOperator()));
        return this;
    }

    /**
     * @Desc 设置limit
     **/
    public QueryBuilder setLimit(long offset, long count){
        queryParam.setLimit(new Limit(offset, count));
        return this;
    }


    /**
     * @Desc 添加排序规则
     **/
    public QueryBuilder addOrder(String field, QueryOrderEnum orderEnum){
        Assert.hasText(field,"field is blank");
        Assert.notNull(orderEnum,"orderEnum is null");
        if (queryParam.getOrders()==null) queryParam.setOrders(new LinkedList<>());
        queryParam.getOrders().add(new Order(field, orderNum++, orderEnum.getOrderStr()));
        return this;
    }


    public QueryParam getQueryParam() {
        return this.queryParam;
    }



    public static void main(String[] args){
        QueryParam queryParam = QueryBuilder.newBuilder()
                .addLikeQuery("name","111", true, true,false)
                .addRangeQuery("code","1","100",true,false)
                .addInQuery("id", Arrays.asList("111","222","333"))
                .setLimit(0,100)
                .addOrder("id",QueryOrderEnum.ASC)
                .addOrder("name",QueryOrderEnum.DESC)
                .getQueryParam();
        System.out.println(JSONUtil.toJsonStr(queryParam));
    }
}
