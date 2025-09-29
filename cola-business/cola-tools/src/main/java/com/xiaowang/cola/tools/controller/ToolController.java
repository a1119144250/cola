package com.xiaowang.cola.tools.controller;

import com.xiaowang.cola.base.response.PageResponse;
import com.xiaowang.cola.tools.domain.entity.Tool;
import com.xiaowang.cola.tools.domain.resp.JsonFormatResponse;
import com.xiaowang.cola.tools.domain.service.ToolService;
import com.xiaowang.cola.tools.param.JsonFormatParam;
import com.xiaowang.cola.tools.param.ToolCreateParam;
import com.xiaowang.cola.tools.param.ToolUpdateParam;
import com.xiaowang.cola.web.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 工具管理控制器
 *
 * @author cola
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("tool")
public class ToolController {

    @Autowired
    private ToolService toolService;

    /**
     * 创建工具
     */
    @PostMapping("/create")
    public Result<Tool> createTool(@Valid @RequestBody ToolCreateParam param) {
        Tool tool = toolService.createTool(
                param.getToolName(),
                param.getDescription(),
                param.getToolType(),
                param.getVersion(),
                param.getCreator()
        );
        return Result.success(tool);
    }

    /**
     * 更新工具信息
     */
    @PostMapping("/update")
    public Result<Boolean> updateTool(@Valid @RequestBody ToolUpdateParam param) {
        boolean result = toolService.updateTool(
                param.getToolId(),
                param.getDescription(),
                param.getConfigInfo(),
                param.getModifier()
        );
        return Result.success(result);
    }

    /**
     * 启用工具
     */
    @PostMapping("/enable/{toolId}")
    public Result<Boolean> enableTool(@PathVariable Long toolId, @RequestParam String modifier) {
        boolean result = toolService.enableTool(toolId, modifier);
        return Result.success(result);
    }

    /**
     * 禁用工具
     */
    @PostMapping("/disable/{toolId}")
    public Result<Boolean> disableTool(@PathVariable Long toolId, @RequestParam String modifier) {
        boolean result = toolService.disableTool(toolId, modifier);
        return Result.success(result);
    }

    /**
     * 根据ID查询工具
     */
    @GetMapping("/{toolId}")
    public Result<Tool> getToolById(@PathVariable Long toolId) {
        Tool tool = toolService.findById(toolId);
        return Result.success(tool);
    }

    /**
     * 根据工具名称查询
     */
    @GetMapping("/name/{toolName}")
    public Result<Tool> getToolByName(@PathVariable String toolName) {
        Tool tool = toolService.findByToolName(toolName);
        return Result.success(tool);
    }

    /**
     * 分页查询工具列表
     */
    @GetMapping("/page")
    public Result<PageResponse<Tool>> pageQuery(
            @RequestParam(required = false) String toolName,
            @RequestParam(required = false) String toolType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int currentPage,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        PageResponse<Tool> result = toolService.pageQuery(toolName, toolType, status, currentPage, pageSize);
        return Result.success(result);
    }

    /**
     * 获取所有启用的工具
     */
    @GetMapping("/enabled")
    public Result<List<Tool>> getAllEnabledTools() {
        List<Tool> tools = toolService.getAllEnabledTools();
        return Result.success(tools);
    }

    /**
     * 根据类型获取工具列表
     */
    @GetMapping("/type/{toolType}")
    public Result<List<Tool>> getToolsByType(@PathVariable String toolType) {
        List<Tool> tools = toolService.getToolsByType(toolType);
        return Result.success(tools);
    }

    /**
     * JSON 格式化工具
     */
    @PostMapping("/json/format")
    public Result<JsonFormatResponse> formatJson(@Valid @RequestBody JsonFormatParam param) {
        log.info("接收到 JSON 格式化请求，格式类型: {}, JSON 长度: {}", 
                param.getFormatType(), param.getJsonStr().length());
        
        JsonFormatResponse response = toolService.formatJson(param);
        
        if (response.getIsValid()) {
            log.info("JSON 格式化成功，原始大小: {} bytes, 格式化后大小: {} bytes, 压缩比: {}", 
                    response.getOriginalSize(), response.getFormattedSize(), response.getCompressionRatio());
        } else {
            log.warn("JSON 格式化失败: {}", response.getErrorMessage());
        }
        
        return Result.success(response);
    }
}
