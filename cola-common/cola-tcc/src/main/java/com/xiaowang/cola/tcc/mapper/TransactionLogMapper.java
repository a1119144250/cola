package com.xiaowang.cola.tcc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaowang.cola.tcc.entity.TransactionLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author cola
 * 事务日志
 */
@Mapper
public interface TransactionLogMapper extends BaseMapper<TransactionLog> {



}
