package com.xiaowang.cola.tools.constant;

/**
 * 防重Token常量
 *
 * @author cola
 */
public class SubmitTokenConstant {

  /**
   * Token前缀
   */
  private static final String TOKEN_PREFIX = "token:";

  /**
   * Token有效期（秒） - 默认5分钟
   */
  public static final long TOKEN_EXPIRE_TIME = 300;

  /**
   * Token长度
   */
  public static final int TOKEN_LENGTH = 32;

  /**
   * 获取Token前缀
   */
  public static String getTokenPrefix() {
    return TOKEN_PREFIX;
  }
}





