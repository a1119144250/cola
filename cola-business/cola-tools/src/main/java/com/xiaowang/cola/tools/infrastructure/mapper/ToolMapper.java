package com.xiaowang.cola.tools.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaowang.cola.tools.domain.entity.Tool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 工具数据访问层
 *
 * @author cola
 */
@Mapper
public interface ToolMapper extends BaseMapper<Tool> {

    /**
     * 根据ID查询工具
     *
     * @param id 工具ID
     * @return 工具信息
     */
    @Select("SELECT * FROM tool WHERE id = #{id}")
    Tool findById(@Param("id") Long id);

    /**
     * 根据工具名称查询
     *
     * @param toolName 工具名称
     * @return 工具信息
     */
    @Select("SELECT * FROM tool WHERE tool_name = #{toolName}")
    Tool findByToolName(@Param("toolName") String toolName);
}
