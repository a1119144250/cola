package com.xiaowang.cola.tools.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 库存流水记录实体
 *
 * @author cola
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockRecord {

  /**
   * 流水记录ID
   */
  private String recordId;

  /**
   * 商品ID
   */
  private String productId;

  /**
   * 操作类型（DEDUCT-扣减, ADD-增加, FREEZE-冻结, UNFREEZE-解冻）
   */
  private String operationType;

  /**
   * 操作数量
   */
  private Integer amount;

  /**
   * 操作前库存
   */
  private Integer beforeStock;

  /**
   * 操作后库存
   */
  private Integer afterStock;

  /**
   * 用户ID
   */
  private String userId;

  /**
   * 订单ID
   */
  private String orderId;

  /**
   * 业务场景
   */
  private String scene;

  /**
   * 流水状态
   */
  private String status;

  /**
   * 扩展信息
   */
  private String extInfo;

  /**
   * 创建时间
   */
  private LocalDateTime createTime;

  /**
   * 更新时间
   */
  private LocalDateTime updateTime;

  /**
   * 对账时间
   */
  private LocalDateTime reconcileTime;

  /**
   * 备注
   */
  private String remark;
}
