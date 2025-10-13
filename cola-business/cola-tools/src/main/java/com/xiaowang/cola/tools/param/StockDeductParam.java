package com.xiaowang.cola.tools.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 库存扣减参数
 *
 * @author cola
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDeductParam {

  /**
   * 商品ID
   */
  @NotBlank(message = "商品ID不能为空")
  private String productId;

  /**
   * 扣减数量
   */
  @NotNull(message = "扣减数量不能为空")
  @Min(value = 1, message = "扣减数量必须大于0")
  private Integer amount;

  /**
   * 用户ID
   */
  @NotBlank(message = "用户ID不能为空")
  private String userId;

  /**
   * 订单ID（可选，用于关联订单）
   */
  private String orderId;

  /**
   * 业务场景（如：秒杀、普通下单等）
   */
  @NotBlank(message = "业务场景不能为空")
  private String scene;

  /**
   * 扩展信息（JSON格式）
   */
  private String extInfo;

  /**
   * 流水过期时间（秒），不传则使用默认值
   */
  private Integer recordExpireTime;
}
