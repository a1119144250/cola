package com.xiaowang.cola.tools.controller;

import com.xiaowang.cola.tools.param.SubmitTokenGenerateParam;
import com.xiaowang.cola.tools.param.SubmitTokenValidateParam;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.alibaba.fastjson.JSON;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 防重Token控制器测试
 *
 * @author cola
 */
@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
public class SubmitTokenControllerTest {

  @Autowired
  private MockMvc mockMvc;

  /**
   * 测试生成Token接口
   */
  @Test
  public void testGenerateToken() throws Exception {
    SubmitTokenGenerateParam param = new SubmitTokenGenerateParam();
    param.setScene("order");
    param.setUserId("123456");
    param.setBizId("test-order");

    MvcResult result = mockMvc.perform(post("/submit-token/generate")
        .contentType(MediaType.APPLICATION_JSON)
        .content(JSON.toJSONString(param)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("SUCCESS"))
        .andExpect(jsonPath("$.data.token").exists())
        .andReturn();

    log.info("生成Token接口测试成功: {}", result.getResponse().getContentAsString());
  }

  /**
   * 测试验证Token接口
   */
  @Test
  public void testValidateToken() throws Exception {
    // 1. 先生成Token
    SubmitTokenGenerateParam generateParam = new SubmitTokenGenerateParam();
    generateParam.setScene("payment");
    generateParam.setUserId("789012");

    MvcResult generateResult = mockMvc.perform(post("/submit-token/generate")
        .contentType(MediaType.APPLICATION_JSON)
        .content(JSON.toJSONString(generateParam)))
        .andExpect(status().isOk())
        .andReturn();

    String generateResponse = generateResult.getResponse().getContentAsString();
    log.info("生成Token响应: {}", generateResponse);

    // 提取token（简化处理，实际应该解析JSON）
    String token = extractTokenFromResponse(generateResponse);

    // 2. 验证Token
    SubmitTokenValidateParam validateParam = new SubmitTokenValidateParam();
    validateParam.setScene("payment");
    validateParam.setUserId("789012");
    validateParam.setToken(token);

    mockMvc.perform(post("/submit-token/validate")
        .contentType(MediaType.APPLICATION_JSON)
        .content(JSON.toJSONString(validateParam)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("SUCCESS"))
        .andExpect(jsonPath("$.data.valid").value(true));

    log.info("验证Token接口测试成功");
  }

  /**
   * 测试删除Token接口
   */
  @Test
  public void testDeleteToken() throws Exception {
    // 1. 先生成Token
    SubmitTokenGenerateParam generateParam = new SubmitTokenGenerateParam();
    generateParam.setScene("delete-test");
    generateParam.setUserId("333333");
    generateParam.setBizId("delete-biz");

    mockMvc.perform(post("/submit-token/generate")
        .contentType(MediaType.APPLICATION_JSON)
        .content(JSON.toJSONString(generateParam)))
        .andExpect(status().isOk());

    // 2. 删除Token
    mockMvc.perform(delete("/submit-token/delete")
        .param("scene", "delete-test")
        .param("userId", "333333")
        .param("bizId", "delete-biz"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("SUCCESS"))
        .andExpect(jsonPath("$.data").value(true));

    log.info("删除Token接口测试成功");
  }

  /**
   * 测试参数校验
   */
  @Test
  public void testParameterValidation() throws Exception {
    // 缺少必填参数
    SubmitTokenGenerateParam param = new SubmitTokenGenerateParam();
    param.setScene("order");
    // 缺少userId

    mockMvc.perform(post("/submit-token/generate")
        .contentType(MediaType.APPLICATION_JSON)
        .content(JSON.toJSONString(param)))
        .andExpect(status().is4xxClientError());

    log.info("参数校验测试通过");
  }

  /**
   * 从响应中提取token（简化版）
   */
  private String extractTokenFromResponse(String response) {
    try {
      com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(response);
      return jsonObject.getJSONObject("data").getString("token");
    } catch (Exception e) {
      log.error("提取token失败", e);
      return null;
    }
  }
}
