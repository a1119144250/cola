package com.xiaowang.cola.tools.example;

import com.xiaowang.cola.tools.domain.resp.SubmitTokenResponse;
import com.xiaowang.cola.tools.domain.resp.SubmitTokenValidateResponse;
import com.xiaowang.cola.tools.domain.service.SubmitTokenService;
import com.xiaowang.cola.tools.param.SubmitTokenGenerateParam;
import com.xiaowang.cola.tools.param.SubmitTokenValidateParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 订单提交示例 - 演示如何在业务中使用防重Token
 * 
 * 这是一个完整的订单提交流程示例，展示了：
 * 1. 用户进入订单页面时生成Token
 * 2. 用户提交订单时验证Token
 * 3. Token验证通过后执行业务逻辑
 *
 * @author cola
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSubmitExample {

  private final SubmitTokenService submitTokenService;

  /**
   * 步骤1: 用户进入订单确认页面，生成防重Token
   * 
   * 调用时机：用户点击"去结算"按钮，进入订单确认页面时
   * 
   * @param userId 用户ID
   * @return 订单页面初始化响应（包含Token）
   */
  public OrderPageInitResponse initOrderPage(String userId) {
    log.info("用户进入订单页面，开始生成防重Token, userId={}", userId);

    // 生成防重Token
    SubmitTokenGenerateParam param = new SubmitTokenGenerateParam();
    param.setScene("order"); // 业务场景：订单
    param.setUserId(userId);
    param.setBizId("order-submit"); // 具体业务：订单提交

    SubmitTokenResponse tokenResponse = submitTokenService.generateToken(param);

    log.info("防重Token生成成功, userId={}, token={}", userId, tokenResponse.getToken());

    // 返回订单页面初始化数据（包含Token）
    return OrderPageInitResponse.builder()
        .token(tokenResponse.getToken())
        .tokenExpireTime(tokenResponse.getExpireTime())
        .message("订单页面初始化成功，请在5分钟内完成提交")
        .build();
  }

  /**
   * 步骤2: 用户点击"提交订单"按钮，验证Token并创建订单
   * 
   * 调用时机：用户点击"提交订单"按钮时
   * 
   * @param request 订单提交请求（包含Token）
   * @return 订单创建结果
   */
  @Transactional(rollbackFor = Exception.class)
  public OrderSubmitResponse submitOrder(OrderSubmitRequest request) {
    log.info("收到订单提交请求, userId={}, token={}", request.getUserId(), request.getToken());

    // 1. 验证防重Token（核心步骤）
    SubmitTokenValidateParam validateParam = new SubmitTokenValidateParam();
    validateParam.setScene("order");
    validateParam.setUserId(request.getUserId());
    validateParam.setToken(request.getToken());

    SubmitTokenValidateResponse validateResponse = submitTokenService.validateToken(validateParam);

    // 2. Token验证失败，拒绝请求
    if (!validateResponse.getValid()) {
      log.warn("Token验证失败，拒绝订单提交, userId={}, reason={}",
          request.getUserId(), validateResponse.getMessage());

      return OrderSubmitResponse.builder()
          .success(false)
          .message("订单提交失败：" + validateResponse.getMessage())
          .needRefreshToken(true) // 提示前端需要重新获取Token
          .build();
    }

    log.info("Token验证通过，开始创建订单, userId={}", request.getUserId());

    // 3. Token验证成功，执行业务逻辑
    try {
      // 3.1 校验商品信息
      validateProduct(request);

      // 3.2 校验库存
      checkStock(request);

      // 3.3 计算订单金额
      BigDecimal totalAmount = calculateTotalAmount(request);

      // 3.4 创建订单
      String orderId = createOrder(request, totalAmount);

      // 3.5 扣减库存
      deductStock(request);

      // 3.6 创建支付单
      String paymentId = createPayment(orderId, totalAmount);

      log.info("订单创建成功, userId={}, orderId={}, amount={}",
          request.getUserId(), orderId, totalAmount);

      return OrderSubmitResponse.builder()
          .success(true)
          .orderId(orderId)
          .paymentId(paymentId)
          .totalAmount(totalAmount)
          .message("订单提交成功")
          .needRefreshToken(false)
          .build();

    } catch (Exception e) {
      log.error("订单创建失败, userId={}, error={}", request.getUserId(), e.getMessage(), e);

      // 注意：业务失败后，Token已被删除，用户需要重新获取
      return OrderSubmitResponse.builder()
          .success(false)
          .message("订单创建失败：" + e.getMessage())
          .needRefreshToken(true) // 提示前端需要重新获取Token
          .build();
    }
  }

  /**
   * 步骤3: 如果用户取消订单或其他场景，手动清除Token
   * 
   * @param userId 用户ID
   * @return 是否清除成功
   */
  public boolean cancelOrderToken(String userId) {
    log.info("取消订单，清除Token, userId={}", userId);
    return submitTokenService.deleteToken("order", userId, "order-submit");
  }

  // ==================== 以下是模拟的业务方法 ====================

  private void validateProduct(OrderSubmitRequest request) {
    // 模拟：校验商品是否存在、是否下架等
    log.info("校验商品信息...");
  }

  private void checkStock(OrderSubmitRequest request) {
    // 模拟：检查库存是否充足
    log.info("检查库存...");
  }

  private BigDecimal calculateTotalAmount(OrderSubmitRequest request) {
    // 模拟：计算订单总金额
    log.info("计算订单金额...");
    return request.getTotalAmount();
  }

  private String createOrder(OrderSubmitRequest request, BigDecimal totalAmount) {
    // 模拟：创建订单记录
    log.info("创建订单记录...");
    return "ORDER" + System.currentTimeMillis();
  }

  private void deductStock(OrderSubmitRequest request) {
    // 模拟：扣减库存
    log.info("扣减库存...");
  }

  private String createPayment(String orderId, BigDecimal amount) {
    // 模拟：创建支付单
    log.info("创建支付单...");
    return "PAY" + System.currentTimeMillis();
  }

  // ==================== 数据对象定义 ====================

  /**
   * 订单页面初始化响应
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderPageInitResponse {
    /**
     * 防重Token
     */
    private String token;

    /**
     * Token有效期（秒）
     */
    private Long tokenExpireTime;

    /**
     * 提示信息
     */
    private String message;
  }

  /**
   * 订单提交请求
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderSubmitRequest {
    /**
     * 防重Token（必填）
     */
    private String token;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 收货地址ID
     */
    private String addressId;

    /**
     * 备注
     */
    private String remark;
  }

  /**
   * 订单提交响应
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class OrderSubmitResponse {
    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 支付单ID
     */
    private String paymentId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 是否需要重新获取Token
     */
    private Boolean needRefreshToken;
  }
}
