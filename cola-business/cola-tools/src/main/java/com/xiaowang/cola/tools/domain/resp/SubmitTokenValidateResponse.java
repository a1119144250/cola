package com.xiaowang.cola.tools.domain.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 防重Token验证响应
 *
 * @author cola
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitTokenValidateResponse {

  /**
   * 是否验证通过
   */
  private Boolean valid;

  /**
   * 验证结果消息
   */
  private String message;

  /**
   * Token
   */
  private String token;
}






