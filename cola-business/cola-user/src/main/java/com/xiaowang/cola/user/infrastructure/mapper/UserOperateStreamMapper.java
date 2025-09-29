package com.xiaowang.cola.user.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaowang.cola.user.domain.entity.UserOperateStream;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户操作流水表 Mapper 接口
 * </p>
 *
 * @author cola
 */
@Mapper
public interface UserOperateStreamMapper extends BaseMapper<UserOperateStream> {

}
