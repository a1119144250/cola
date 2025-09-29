package com.xiaowang.cola.tools.domain.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaowang.cola.base.response.PageResponse;
import com.xiaowang.cola.tools.domain.entity.Tool;
import com.xiaowang.cola.tools.domain.resp.JsonFormatResponse;
import com.xiaowang.cola.tools.infrastructure.exception.ToolErrorCode;
import com.xiaowang.cola.tools.infrastructure.exception.ToolException;
import com.xiaowang.cola.tools.infrastructure.mapper.ToolMapper;
import com.xiaowang.cola.tools.param.JsonFormatParam;
import com.xiaowang.cola.tools.utils.ToolConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.xiaowang.cola.tools.infrastructure.exception.ToolErrorCode.DUPLICATE_TOOL_NAME;
import static com.xiaowang.cola.tools.infrastructure.exception.ToolErrorCode.TOOL_NOT_EXIST;

/**
 * 工具服务
 *
 * @author cola
 */
@Slf4j
@Service
public class ToolService extends ServiceImpl<ToolMapper, Tool> {

    @Autowired
    private ToolMapper toolMapper;

    /**
     * 创建工具
     *
     * @param toolName    工具名称
     * @param description 工具描述
     * @param toolType    工具类型
     * @param version     工具版本
     * @param creator     创建者
     * @return 工具信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Tool createTool(String toolName, String description, String toolType, String version, String creator) {
        // 检查工具名称是否已存在
        Tool existTool = toolMapper.findByToolName(toolName);
        if (existTool != null) {
            throw new ToolException(DUPLICATE_TOOL_NAME);
        }

        Tool tool = new Tool();
        tool.init(toolName, description, toolType, version, creator);
        
        boolean result = save(tool);
        Assert.isTrue(result, () -> new ToolException(ToolErrorCode.TOOL_OPERATE_FAILED));
        
        return tool;
    }

    /**
     * 更新工具信息
     *
     * @param toolId      工具ID
     * @param description 工具描述
     * @param configInfo  配置信息
     * @param modifier    修改者
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTool(Long toolId, String description, String configInfo, String modifier) {
        Tool tool = toolMapper.findById(toolId);
        Assert.notNull(tool, () -> new ToolException(TOOL_NOT_EXIST));

        tool.updateInfo(description, configInfo, modifier);
        return updateById(tool);
    }

    /**
     * 启用工具
     *
     * @param toolId   工具ID
     * @param modifier 修改者
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean enableTool(Long toolId, String modifier) {
        Tool tool = toolMapper.findById(toolId);
        Assert.notNull(tool, () -> new ToolException(TOOL_NOT_EXIST));

        tool.enable(modifier);
        return updateById(tool);
    }

    /**
     * 禁用工具
     *
     * @param toolId   工具ID
     * @param modifier 修改者
     * @return 是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean disableTool(Long toolId, String modifier) {
        Tool tool = toolMapper.findById(toolId);
        Assert.notNull(tool, () -> new ToolException(TOOL_NOT_EXIST));

        tool.disable(modifier);
        return updateById(tool);
    }

    /**
     * 根据ID查询工具
     *
     * @param toolId 工具ID
     * @return 工具信息
     */
    public Tool findById(Long toolId) {
        return toolMapper.findById(toolId);
    }

    /**
     * 根据工具名称查询
     *
     * @param toolName 工具名称
     * @return 工具信息
     */
    public Tool findByToolName(String toolName) {
        return toolMapper.findByToolName(toolName);
    }

    /**
     * 分页查询工具列表
     *
     * @param toolName    工具名称（可选）
     * @param toolType    工具类型（可选）
     * @param status      状态（可选）
     * @param currentPage 当前页
     * @param pageSize    每页大小
     * @return 分页结果
     */
    public PageResponse<Tool> pageQuery(String toolName, String toolType, Integer status, int currentPage, int pageSize) {
        Page<Tool> page = new Page<>(currentPage, pageSize);
        QueryWrapper<Tool> wrapper = new QueryWrapper<>();

        if (StrUtil.isNotBlank(toolName)) {
            wrapper.like("tool_name", toolName);
        }
        if (StrUtil.isNotBlank(toolType)) {
            wrapper.eq("tool_type", toolType);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.orderBy(true, false, "gmt_create");

        Page<Tool> toolPage = this.page(page, wrapper);
        return PageResponse.of(toolPage.getRecords(), (int) toolPage.getTotal(), pageSize, currentPage);
    }

    /**
     * 获取所有启用的工具
     *
     * @return 工具列表
     */
    public List<Tool> getAllEnabledTools() {
        QueryWrapper<Tool> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderBy(true, false, "gmt_create");
        return list(wrapper);
    }

    /**
     * 根据类型获取工具列表
     *
     * @param toolType 工具类型
     * @return 工具列表
     */
    public List<Tool> getToolsByType(String toolType) {
        QueryWrapper<Tool> wrapper = new QueryWrapper<>();
        wrapper.eq("tool_type", toolType);
        wrapper.eq("status", 1);
        wrapper.orderBy(true, false, "gmt_create");
        return list(wrapper);
    }

    /**
     * JSON 格式化
     *
     * @param param 格式化参数
     * @return 格式化结果
     */
    public JsonFormatResponse formatJson(JsonFormatParam param) {
        long startTime = System.currentTimeMillis();
        long originalSize = (long) param.getJsonStr().getBytes(StandardCharsets.UTF_8).length;
        
        JsonFormatResponse.JsonFormatResponseBuilder responseBuilder = JsonFormatResponse.builder()
                .formatType(param.getFormatType())
                .originalSize(originalSize);

        try {
            // 验证 JSON 语法
            Object parsedJson = null;
            if (param.getValidate()) {
                // 先尝试解析为对象，如果失败则尝试解析为数组
                try {
                    parsedJson = JSON.parseObject(param.getJsonStr());
                } catch (JSONException e) {
                    parsedJson = JSON.parseArray(param.getJsonStr());
                }
            } else {
                // 不验证时也需要解析以便格式化
                try {
                    parsedJson = JSON.parseObject(param.getJsonStr());
                } catch (JSONException e) {
                    parsedJson = JSON.parseArray(param.getJsonStr());
                }
            }

            String formattedJson = formatJsonByType(parsedJson, param.getFormatType(), param.getIndentSize(), param.getSortKeys());
            long formattedSize = formattedJson.getBytes(StandardCharsets.UTF_8).length;
            double compressionRatio = calculateCompressionRatio(originalSize, formattedSize);

            responseBuilder
                    .formattedJson(formattedJson)
                    .formattedSize(formattedSize)
                    .compressionRatio(compressionRatio)
                    .isValid(true);

        } catch (JSONException e) {
            log.warn("JSON 格式化失败，输入的 JSON 字符串格式不正确: {}", e.getMessage());
            responseBuilder
                    .formattedJson(param.getJsonStr())
                    .formattedSize(originalSize)
                    .compressionRatio(1.0)
                    .isValid(false)
                    .errorMessage("JSON 格式不正确: " + e.getMessage());
        } catch (Exception e) {
            log.error("JSON 格式化处理异常", e);
            responseBuilder
                    .formattedJson(param.getJsonStr())
                    .formattedSize(originalSize)
                    .compressionRatio(1.0)
                    .isValid(false)
                    .errorMessage("处理异常: " + e.getMessage());
        }

        long processTime = System.currentTimeMillis() - startTime;
        responseBuilder.processTime(processTime);

        return responseBuilder.build();
    }

    /**
     * 根据类型格式化 JSON
     */
    private String formatJsonByType(Object jsonObj, String formatType, Integer indentSize, Boolean sortKeys) {
        switch (formatType) {
            case ToolConstants.JsonFormatType.PRETTY:
                return formatPrettyJson(jsonObj, indentSize, sortKeys);
            case ToolConstants.JsonFormatType.COMPACT:
                return formatCompactJson(jsonObj, sortKeys);
            case ToolConstants.JsonFormatType.MINIFY:
                return formatMinifyJson(jsonObj, sortKeys);
            default:
                return formatPrettyJson(jsonObj, indentSize, sortKeys);
        }
    }

    /**
     * 美化格式化
     */
    private String formatPrettyJson(Object jsonObj, Integer indentSize, Boolean sortKeys) {
        List<JSONWriter.Feature> featureList = new ArrayList<>();
        featureList.add(JSONWriter.Feature.PrettyFormat);
        
        if (sortKeys != null && sortKeys) {
            featureList.add(JSONWriter.Feature.MapSortField);
        }
        
        JSONWriter.Feature[] features = featureList.toArray(new JSONWriter.Feature[0]);
        String result = JSON.toJSONString(jsonObj, features);
        
        // 处理自定义缩进大小
        if (indentSize != null && indentSize > 0) {
            result = adjustIndentation(result, indentSize);
        }
        
        return result;
    }

    /**
     * 紧凑格式化
     */
    private String formatCompactJson(Object jsonObj, Boolean sortKeys) {
        JSONWriter.Feature[] features = sortKeys 
                ? new JSONWriter.Feature[]{JSONWriter.Feature.MapSortField}
                : new JSONWriter.Feature[]{};
        
        return JSON.toJSONString(jsonObj, features);
    }

    /**
     * 压缩格式化
     */
    private String formatMinifyJson(Object jsonObj, Boolean sortKeys) {
        JSONWriter.Feature[] features = sortKeys 
                ? new JSONWriter.Feature[]{JSONWriter.Feature.MapSortField}
                : new JSONWriter.Feature[]{};
        
        return JSON.toJSONString(jsonObj, features);
    }

    /**
     * 调整缩进大小
     */
    private String adjustIndentation(String json, int indentSize) {
        if (indentSize <= 0) {
            return json;
        }
        
        String[] lines = json.split("\n");
        StringBuilder result = new StringBuilder();
        String customIndent = " ".repeat(indentSize);
        int currentLevel = 0;
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                result.append("\n");
                continue;
            }
            
            // 如果是结束符号，先减少级别
            if (trimmedLine.equals("}") || trimmedLine.equals("]") || 
                trimmedLine.endsWith("}") || trimmedLine.endsWith("]")) {
                currentLevel = Math.max(0, currentLevel - 1);
            }
            
            // 添加缩进
            result.append(customIndent.repeat(currentLevel)).append(trimmedLine).append("\n");
            
            // 如果是开始符号，增加级别
            if (trimmedLine.equals("{") || trimmedLine.equals("[") || 
                trimmedLine.endsWith("{") || trimmedLine.endsWith("[")) {
                currentLevel++;
            }
        }
        
        return result.toString().trim();
    }

    /**
     * 计算压缩比例
     */
    private double calculateCompressionRatio(long originalSize, long formattedSize) {
        if (originalSize == 0) {
            return 1.0;
        }
        return (double) formattedSize / originalSize;
    }
}
