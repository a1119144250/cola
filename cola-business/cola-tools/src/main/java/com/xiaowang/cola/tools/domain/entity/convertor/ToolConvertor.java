package com.xiaowang.cola.tools.domain.entity.convertor;

import com.xiaowang.cola.tools.domain.entity.Tool;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 工具转换器
 *
 * @author cola
 */
@Mapper
public interface ToolConvertor {

    ToolConvertor INSTANCE = Mappers.getMapper(ToolConvertor.class);

    /**
     * 实体转VO
     * 
     * @param tool 工具实体
     * @return ToolVO
     */
    // 这里可以根据实际需要添加转换方法
    // ToolVO mapToVo(Tool tool);
}
