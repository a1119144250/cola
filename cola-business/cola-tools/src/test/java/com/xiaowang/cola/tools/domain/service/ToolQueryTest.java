package com.xiaowang.cola.tools.domain.service;

import com.xiaowang.cola.tools.domain.entity.Tool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工具查询测试（基于预插入的数据）
 *
 * @author cola
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ToolQueryTest {

    @Autowired
    private ToolService toolService;

    @Test
    void testFindByIdFromPreloadedData() {
        // 从预加载的数据中查询（应该有5条记录，ID从1开始）
        Tool tool1 = toolService.findById(1L);
        assertNotNull(tool1);
        assertEquals("字符串工具", tool1.getToolName());
        assertEquals("SYSTEM", tool1.getToolType());
        assertEquals("1.0.0", tool1.getVersion());
        assertEquals(1, tool1.getStatus());
        
        System.out.println("找到预加载的工具:");
        System.out.println("ID: " + tool1.getId());
        System.out.println("工具名称: " + tool1.getToolName());
        System.out.println("描述: " + tool1.getDescription());
        System.out.println("类型: " + tool1.getToolType());
        System.out.println("版本: " + tool1.getVersion());
        System.out.println("状态: " + tool1.getStatus());
    }

    @Test
    void testFindByIdNotFound() {
        // 查询不存在的ID
        Tool notFoundTool = toolService.findById(99999L);
        assertNull(notFoundTool);
        System.out.println("查询不存在的ID 99999，返回结果: " + notFoundTool);
    }

    @Test
    void testFindByToolName() {
        // 根据工具名称查询
        Tool tool = toolService.findByToolName("JSON工具");
        assertNotNull(tool);
        assertEquals("JSON工具", tool.getToolName());
        assertEquals("DEVELOPMENT", tool.getToolType());
        
        System.out.println("根据名称查询到的工具:");
        System.out.println("ID: " + tool.getId());
        System.out.println("工具名称: " + tool.getToolName());
        System.out.println("描述: " + tool.getDescription());
        System.out.println("类型: " + tool.getToolType());
    }

    @Test
    void testFindAllPreloadedTools() {
        // 测试查询所有预加载的工具
        for (long id = 1; id <= 5; id++) {
            Tool tool = toolService.findById(id);
            if (tool != null) {
                System.out.println("工具 " + id + ": " + tool.getToolName() + " - " + tool.getToolType());
            }
        }
    }

    @Test
    void testGetToolsByType() {
        // 测试根据类型查询工具
        var systemTools = toolService.getToolsByType("SYSTEM");
        assertNotNull(systemTools);
        assertTrue(systemTools.size() > 0);
        
        System.out.println("SYSTEM类型的工具:");
        systemTools.forEach(tool -> 
            System.out.println("- " + tool.getToolName() + " (ID: " + tool.getId() + ")")
        );

        var devTools = toolService.getToolsByType("DEVELOPMENT");
        assertNotNull(devTools);
        assertTrue(devTools.size() > 0);
        
        System.out.println("DEVELOPMENT类型的工具:");
        devTools.forEach(tool -> 
            System.out.println("- " + tool.getToolName() + " (ID: " + tool.getId() + ")")
        );
    }
}
