package com.xiaowang.cola.tools.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量删除库存流水参数
 *
 * @author cola
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockRecordBatchDeleteParam {

  /**
   * 商品ID
   */
  @NotBlank(message = "商品ID不能为空")
  private String productId;

  /**
   * 流水记录ID列表
   */
  @NotEmpty(message = "流水记录ID列表不能为空")
  private List<String> recordIds;
}
