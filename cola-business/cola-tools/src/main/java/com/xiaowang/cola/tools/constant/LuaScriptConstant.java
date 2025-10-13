package com.xiaowang.cola.tools.constant;

/**
 * Lua脚本常量
 *
 * @author cola
 */
public class LuaScriptConstant {

  /**
   * Token验证和删除脚本（原子操作）
   * KEYS[1]: token的完整key
   * ARGV[1]: 预期的token值
   * 返回值：1-验证成功并删除, 0-token不存在, -1-token不匹配
   */
  public static final String VALIDATE_AND_DELETE_TOKEN_SCRIPT = "local token = redis.call('get', KEYS[1])\n" +
      "if not token then\n" +
      "    return 0\n" +
      "end\n" +
      "if token == ARGV[1] then\n" +
      "    redis.call('del', KEYS[1])\n" +
      "    return 1\n" +
      "else\n" +
      "    return -1\n" +
      "end";
}
