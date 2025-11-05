package com.xiaowang.cola.tools.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 生成防重Token参数
 *
 * @author cola
 */
@Data
public class SubmitTokenGenerateParam {

  /**
   * 业务场景标识（如：order、payment等）
   */
  @NotBlank(message = "业务场景不能为空")
  private String scene;

  /**
   * 用户ID
   */
  @NotBlank(message = "用户ID不能为空")
  private String userId;

  /**
   * 业务标识（可选，用于更精细的场景区分）
   */
  private String bizId;
}






