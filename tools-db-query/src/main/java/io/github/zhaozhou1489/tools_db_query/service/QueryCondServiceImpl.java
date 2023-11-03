package io.github.zhaozhou1489.tools_db_query.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.zhaozhou1489.tools_db_query.params.QueryCond;
import io.github.zhaozhou1489.tools_db_query.tools.QueryWrapperUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * @Author:zhaozhou
 * @Date: 2023/08/23
 * @Desc: 封装基本的根据QueryCond来查找的方法
 */
public class QueryCondServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M,T> {


    /**
     * @Desc 查找一个
     **/
    public T selectOne(QueryCond cond) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        QueryWrapperUtil.transQueryCond(wrapper, cond);
        List<T> datas = baseMapper.selectList(wrapper);
        return CollectionUtils.isNotEmpty(datas) ? datas.get(0) : null;
    }

    /**
     * @Desc 查找列表
     **/
    public List<T> selectList(QueryCond cond) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        QueryWrapperUtil.transQueryCond(wrapper, cond);
        return baseMapper.selectList(wrapper);
    }

    /**
     * @Desc 分页查找
     **/
    public Page selectPage(QueryCond cond, int pageNo, int pageSize) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        QueryWrapperUtil.transQueryCond(wrapper, cond);
        return baseMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    }
}
