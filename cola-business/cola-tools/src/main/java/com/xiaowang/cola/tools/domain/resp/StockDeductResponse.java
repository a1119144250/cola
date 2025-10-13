package com.xiaowang.cola.tools.domain.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存扣减响应
 *
 * @author cola
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDeductResponse {

  /**
   * 是否扣减成功
   */
  private Boolean success;

  /**
   * 响应消息
   */
  private String message;

  /**
   * 流水记录ID
   */
  private String recordId;

  /**
   * 商品ID
   */
  private String productId;

  /**
   * 扣减数量
   */
  private Integer amount;

  /**
   * 扣减后剩余库存（仅成功时返回）
   */
  private Integer remainingStock;

  /**
   * 响应时间戳
   */
  private Long timestamp;

  /**
   * 创建成功响应
   */
  public static StockDeductResponse success(String recordId, String productId, Integer amount, Integer remainingStock) {
    return StockDeductResponse.builder()
        .success(true)
        .message("库存扣减成功")
        .recordId(recordId)
        .productId(productId)
        .amount(amount)
        .remainingStock(remainingStock)
        .timestamp(System.currentTimeMillis())
        .build();
  }

  /**
   * 创建失败响应
   */
  public static StockDeductResponse failure(String productId, Integer amount, String message) {
    return StockDeductResponse.builder()
        .success(false)
        .message(message)
        .productId(productId)
        .amount(amount)
        .timestamp(System.currentTimeMillis())
        .build();
  }
}
