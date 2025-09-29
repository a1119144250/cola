package com.xiaowang.cola.tools.domain.service;

import com.xiaowang.cola.tools.domain.entity.Tool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工具服务测试
 *
 * @author cola
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ToolServiceTest {

    @Autowired
    private ToolService toolService;

    @Test
    void testFindById() {
        // 先创建一个工具
        Tool createdTool = toolService.createTool(
                "测试工具", 
                "这是一个测试工具", 
                "SYSTEM", 
                "1.0.0", 
                "test_user"
        );
        
        assertNotNull(createdTool);
        assertNotNull(createdTool.getId());
        
        // 根据ID查询工具
        Tool foundTool = toolService.findById(createdTool.getId());
        
        // 验证查询结果
        assertNotNull(foundTool);
        assertEquals(createdTool.getId(), foundTool.getId());
        assertEquals("测试工具", foundTool.getToolName());
        assertEquals("这是一个测试工具", foundTool.getDescription());
        assertEquals("SYSTEM", foundTool.getToolType());
        assertEquals("1.0.0", foundTool.getVersion());
        assertEquals("test_user", foundTool.getCreator());
        assertEquals(1, foundTool.getStatus()); // 默认启用状态
        
        System.out.println("找到的工具信息：");
        System.out.println("ID: " + foundTool.getId());
        System.out.println("工具名称: " + foundTool.getToolName());
        System.out.println("描述: " + foundTool.getDescription());
        System.out.println("类型: " + foundTool.getToolType());
        System.out.println("版本: " + foundTool.getVersion());
        System.out.println("状态: " + foundTool.getStatus());
        System.out.println("创建者: " + foundTool.getCreator());
        System.out.println("创建时间: " + foundTool.getGmtCreate());
    }

    @Test
    void testFindByIdNotFound() {
        // 查询不存在的ID
        Tool notFoundTool = toolService.findById(99999L);
        
        // 应该返回null
        assertNull(notFoundTool);
        System.out.println("查询不存在的ID 99999，返回结果: " + notFoundTool);
    }

    @Test
    void testFindByIdWithMultipleTools() {
        // 创建多个工具
        Tool tool1 = toolService.createTool("工具1", "描述1", "SYSTEM", "1.0", "user1");
        Tool tool2 = toolService.createTool("工具2", "描述2", "DEVELOPMENT", "2.0", "user2");
        Tool tool3 = toolService.createTool("工具3", "描述3", "BUSINESS", "3.0", "user3");
        
        // 分别根据ID查询
        Tool foundTool1 = toolService.findById(tool1.getId());
        Tool foundTool2 = toolService.findById(tool2.getId());
        Tool foundTool3 = toolService.findById(tool3.getId());
        
        // 验证查询结果
        assertNotNull(foundTool1);
        assertEquals("工具1", foundTool1.getToolName());
        assertEquals("SYSTEM", foundTool1.getToolType());
        
        assertNotNull(foundTool2);
        assertEquals("工具2", foundTool2.getToolName());
        assertEquals("DEVELOPMENT", foundTool2.getToolType());
        
        assertNotNull(foundTool3);
        assertEquals("工具3", foundTool3.getToolName());
        assertEquals("BUSINESS", foundTool3.getToolType());
        
        System.out.println("成功查询到3个工具:");
        System.out.println("1. " + foundTool1.getToolName() + " - " + foundTool1.getToolType());
        System.out.println("2. " + foundTool2.getToolName() + " - " + foundTool2.getToolType());
        System.out.println("3. " + foundTool3.getToolName() + " - " + foundTool3.getToolType());
    }
}
