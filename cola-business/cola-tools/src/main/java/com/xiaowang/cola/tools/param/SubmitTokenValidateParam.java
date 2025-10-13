package com.xiaowang.cola.tools.param;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 验证防重Token参数
 *
 * @author cola
 */
@Data
public class SubmitTokenValidateParam {

  /**
   * Token值
   */
  @NotBlank(message = "Token不能为空")
  private String token;

  /**
   * 业务场景标识
   */
  @NotBlank(message = "业务场景不能为空")
  private String scene;

  /**
   * 用户ID
   */
  @NotBlank(message = "用户ID不能为空")
  private String userId;
}


