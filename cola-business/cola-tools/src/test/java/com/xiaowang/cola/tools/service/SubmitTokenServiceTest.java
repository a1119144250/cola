package com.xiaowang.cola.tools.service;

import com.xiaowang.cola.tools.domain.resp.SubmitTokenResponse;
import com.xiaowang.cola.tools.domain.resp.SubmitTokenValidateResponse;
import com.xiaowang.cola.tools.domain.service.SubmitTokenService;
import com.xiaowang.cola.tools.param.SubmitTokenGenerateParam;
import com.xiaowang.cola.tools.param.SubmitTokenValidateParam;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 防重Token服务测试
 *
 * @author cola
 */
@Slf4j
@SpringBootTest
public class SubmitTokenServiceTest {

  @Autowired
  private SubmitTokenService submitTokenService;

  /**
   * 测试生成Token
   */
  @Test
  public void testGenerateToken() {
    SubmitTokenGenerateParam param = new SubmitTokenGenerateParam();
    param.setScene("order");
    param.setUserId("123456");
    param.setBizId("test-order");

    SubmitTokenResponse response = submitTokenService.generateToken(param);

    assertNotNull(response);
    assertNotNull(response.getToken());
    assertEquals(300L, response.getExpireTime());
    assertNotNull(response.getTimestamp());

    log.info("生成Token成功: {}", response.getToken());
  }

  /**
   * 测试Token验证成功场景
   */
  @Test
  public void testValidateTokenSuccess() {
    // 1. 先生成Token
    SubmitTokenGenerateParam generateParam = new SubmitTokenGenerateParam();
    generateParam.setScene("payment");
    generateParam.setUserId("789012");

    SubmitTokenResponse tokenResponse = submitTokenService.generateToken(generateParam);
    String token = tokenResponse.getToken();

    log.info("生成的Token: {}", token);

    // 2. 验证Token
    SubmitTokenValidateParam validateParam = new SubmitTokenValidateParam();
    validateParam.setScene("payment");
    validateParam.setUserId("789012");
    validateParam.setToken(token);

    SubmitTokenValidateResponse response = submitTokenService.validateToken(validateParam);

    assertNotNull(response);
    assertTrue(response.getValid());
    assertEquals("验证通过", response.getMessage());

    log.info("Token验证成功");
  }

  /**
   * 测试Token重复使用（应该失败）
   */
  @Test
  public void testValidateTokenDuplicate() {
    // 1. 生成Token
    SubmitTokenGenerateParam generateParam = new SubmitTokenGenerateParam();
    generateParam.setScene("form");
    generateParam.setUserId("111111");

    SubmitTokenResponse tokenResponse = submitTokenService.generateToken(generateParam);
    String token = tokenResponse.getToken();

    // 2. 第一次验证（成功）
    SubmitTokenValidateParam validateParam = new SubmitTokenValidateParam();
    validateParam.setScene("form");
    validateParam.setUserId("111111");
    validateParam.setToken(token);

    SubmitTokenValidateResponse firstResponse = submitTokenService.validateToken(validateParam);
    assertTrue(firstResponse.getValid());

    log.info("第一次验证成功");

    // 3. 第二次验证（应该失败，因为Token已被删除）
    SubmitTokenValidateResponse secondResponse = submitTokenService.validateToken(validateParam);
    assertFalse(secondResponse.getValid());
    assertEquals("Token不存在或已过期，请重新获取", secondResponse.getMessage());

    log.info("第二次验证失败（预期行为）: {}", secondResponse.getMessage());
  }

  /**
   * 测试Token不存在
   */
  @Test
  public void testValidateTokenNotExists() {
    SubmitTokenValidateParam validateParam = new SubmitTokenValidateParam();
    validateParam.setScene("order");
    validateParam.setUserId("999999");
    validateParam.setToken("non-existent-token");

    SubmitTokenValidateResponse response = submitTokenService.validateToken(validateParam);

    assertNotNull(response);
    assertFalse(response.getValid());
    assertEquals("Token不存在或已过期，请重新获取", response.getMessage());

    log.info("Token不存在验证失败（预期行为）");
  }

  /**
   * 测试Token不匹配
   */
  @Test
  public void testValidateTokenMismatch() {
    // 1. 生成Token
    SubmitTokenGenerateParam generateParam = new SubmitTokenGenerateParam();
    generateParam.setScene("import");
    generateParam.setUserId("222222");

    submitTokenService.generateToken(generateParam);

    // 2. 使用错误的Token验证
    SubmitTokenValidateParam validateParam = new SubmitTokenValidateParam();
    validateParam.setScene("import");
    validateParam.setUserId("222222");
    validateParam.setToken("wrong-token-value");

    SubmitTokenValidateResponse response = submitTokenService.validateToken(validateParam);

    assertNotNull(response);
    assertFalse(response.getValid());
    assertEquals("Token无效", response.getMessage());

    log.info("Token不匹配验证失败（预期行为）");
  }

  /**
   * 测试删除Token
   */
  @Test
  public void testDeleteToken() {
    // 1. 生成Token
    SubmitTokenGenerateParam generateParam = new SubmitTokenGenerateParam();
    generateParam.setScene("delete-test");
    generateParam.setUserId("333333");
    generateParam.setBizId("delete-biz");

    SubmitTokenResponse tokenResponse = submitTokenService.generateToken(generateParam);

    log.info("生成Token: {}", tokenResponse.getToken());

    // 2. 删除Token
    boolean deleteResult = submitTokenService.deleteToken("delete-test", "333333", "delete-biz");
    assertTrue(deleteResult);

    log.info("Token删除成功");

    // 3. 尝试验证已删除的Token（应该失败）
    SubmitTokenValidateParam validateParam = new SubmitTokenValidateParam();
    validateParam.setScene("delete-test");
    validateParam.setUserId("333333");
    validateParam.setToken(tokenResponse.getToken());

    SubmitTokenValidateResponse response = submitTokenService.validateToken(validateParam);
    assertFalse(response.getValid());

    log.info("已删除Token验证失败（预期行为）");
  }

  /**
   * 测试不同场景的Token隔离
   */
  @Test
  public void testTokenSceneIsolation() {
    String userId = "444444";

    // 1. 在order场景生成Token
    SubmitTokenGenerateParam orderParam = new SubmitTokenGenerateParam();
    orderParam.setScene("order");
    orderParam.setUserId(userId);
    SubmitTokenResponse orderToken = submitTokenService.generateToken(orderParam);

    // 2. 在payment场景生成Token
    SubmitTokenGenerateParam paymentParam = new SubmitTokenGenerateParam();
    paymentParam.setScene("payment");
    paymentParam.setUserId(userId);
    SubmitTokenResponse paymentToken = submitTokenService.generateToken(paymentParam);

    // 3. order场景的Token不能在payment场景使用
    SubmitTokenValidateParam crossValidate = new SubmitTokenValidateParam();
    crossValidate.setScene("payment");
    crossValidate.setUserId(userId);
    crossValidate.setToken(orderToken.getToken());

    SubmitTokenValidateResponse crossResponse = submitTokenService.validateToken(crossValidate);
    assertFalse(crossResponse.getValid());

    log.info("跨场景Token验证失败（预期行为，场景隔离生效）");

    // 4. 验证各自场景的Token都是有效的
    SubmitTokenValidateParam orderValidate = new SubmitTokenValidateParam();
    orderValidate.setScene("order");
    orderValidate.setUserId(userId);
    orderValidate.setToken(orderToken.getToken());

    SubmitTokenValidateResponse orderResponse = submitTokenService.validateToken(orderValidate);
    assertTrue(orderResponse.getValid());

    log.info("order场景Token验证成功");

    SubmitTokenValidateParam paymentValidate = new SubmitTokenValidateParam();
    paymentValidate.setScene("payment");
    paymentValidate.setUserId(userId);
    paymentValidate.setToken(paymentToken.getToken());

    SubmitTokenValidateResponse paymentResponse = submitTokenService.validateToken(paymentValidate);
    assertTrue(paymentResponse.getValid());

    log.info("payment场景Token验证成功");
  }
}






