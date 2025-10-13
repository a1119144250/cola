package com.xiaowang.cola.tools.domain.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 防重Token响应
 *
 * @author cola
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmitTokenResponse {

  /**
   * Token值
   */
  private String token;

  /**
   * 过期时间（秒）
   */
  private Long expireTime;

  /**
   * 生成时间戳
   */
  private Long timestamp;
}
