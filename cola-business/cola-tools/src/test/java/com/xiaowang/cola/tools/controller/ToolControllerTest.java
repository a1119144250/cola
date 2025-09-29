package com.xiaowang.cola.tools.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaowang.cola.tools.domain.entity.Tool;
import com.xiaowang.cola.tools.domain.service.ToolService;
import com.xiaowang.cola.tools.param.ToolCreateParam;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

/**
 * 工具控制器测试
 *
 * @author cola
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ToolControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ToolService toolService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private Tool testTool;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // 创建测试数据
        testTool = toolService.createTool(
                "测试工具API", 
                "用于API测试的工具", 
                "SYSTEM", 
                "1.0.0", 
                "api_test_user"
        );
    }

    @Test
    void testGetToolById() throws Exception {
        // 测试根据ID查询工具
        mockMvc.perform(get("/tool/{toolId}", testTool.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testTool.getId()))
                .andExpect(jsonPath("$.data.toolName").value("测试工具API"))
                .andExpect(jsonPath("$.data.description").value("用于API测试的工具"))
                .andExpect(jsonPath("$.data.toolType").value("SYSTEM"))
                .andExpect(jsonPath("$.data.version").value("1.0.0"))
                .andExpect(jsonPath("$.data.status").value(1))
                .andExpect(jsonPath("$.data.creator").value("api_test_user"));
    }

    @Test
    void testGetToolByIdNotFound() throws Exception {
        // 测试查询不存在的工具ID
        mockMvc.perform(get("/tool/{toolId}", 99999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testGetToolByName() throws Exception {
        // 测试根据工具名称查询
        mockMvc.perform(get("/tool/name/{toolName}", "测试工具API")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testTool.getId()))
                .andExpect(jsonPath("$.data.toolName").value("测试工具API"));
    }

    @Test
    void testCreateAndFindTool() throws Exception {
        // 创建工具参数
        ToolCreateParam createParam = new ToolCreateParam();
        createParam.setToolName("新建测试工具");
        createParam.setDescription("通过API创建的测试工具");
        createParam.setToolType("DEVELOPMENT");
        createParam.setVersion("2.0.0");
        createParam.setCreator("api_creator");

        // 创建工具
        String response = mockMvc.perform(post("/tool/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createParam)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.toolName").value("新建测试工具"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 从响应中提取工具ID
        String toolIdPath = "$.data.id";
        Long createdToolId = Long.valueOf(
            objectMapper.readTree(response).at("/data/id").asText()
        );

        // 根据ID查询刚创建的工具
        mockMvc.perform(get("/tool/{toolId}", createdToolId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(createdToolId))
                .andExpect(jsonPath("$.data.toolName").value("新建测试工具"))
                .andExpect(jsonPath("$.data.description").value("通过API创建的测试工具"))
                .andExpect(jsonPath("$.data.toolType").value("DEVELOPMENT"))
                .andExpect(jsonPath("$.data.version").value("2.0.0"))
                .andExpect(jsonPath("$.data.creator").value("api_creator"));
    }
}
