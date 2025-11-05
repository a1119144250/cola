package com.xiaowang.cola.tools.domain.service;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import com.xiaowang.cola.tools.constant.LuaScriptConstant;
import com.xiaowang.cola.tools.constant.SubmitTokenConstant;
import com.xiaowang.cola.tools.domain.resp.SubmitTokenResponse;
import com.xiaowang.cola.tools.domain.resp.SubmitTokenValidateResponse;
import com.xiaowang.cola.tools.param.SubmitTokenGenerateParam;
import com.xiaowang.cola.tools.param.SubmitTokenValidateParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 防重Token服务
 *
 * @author cola
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitTokenService {

  private final RedisTemplate<String, String> redisTemplate;

  /**
   * 生成防重Token
   *
   * @param param 生成参数
   * @return Token响应
   */
  public SubmitTokenResponse generateToken(SubmitTokenGenerateParam param) {
    // 生成唯一Token
    String token = UUID.randomUUID().toString(true);

    // 构建Redis key
    String redisKey = buildTokenKey(param.getScene(), param.getUserId(), param.getBizId());

    // 存储到Redis，设置过期时间
    redisTemplate.opsForValue().set(
        redisKey,
        token,
        SubmitTokenConstant.TOKEN_EXPIRE_TIME,
        TimeUnit.SECONDS);

    log.info("生成防重Token成功，scene={}, userId={}, bizId={}, token={}",
        param.getScene(), param.getUserId(), param.getBizId(), token);

    return SubmitTokenResponse.builder()
        .token(token)
        .expireTime(SubmitTokenConstant.TOKEN_EXPIRE_TIME)
        .timestamp(System.currentTimeMillis())
        .build();
  }

  /**
   * 验证防重Token（验证成功后会删除Token）
   *
   * @param param 验证参数
   * @return 验证响应
   */
  public SubmitTokenValidateResponse validateToken(SubmitTokenValidateParam param) {
    String redisKey = buildTokenKey(param.getScene(), param.getUserId(), null);

    // 使用Lua脚本保证原子性：验证token并删除
    DefaultRedisScript<Long> script = new DefaultRedisScript<>();
    script.setScriptText(LuaScriptConstant.VALIDATE_AND_DELETE_TOKEN_SCRIPT);
    script.setResultType(Long.class);

    Long result = redisTemplate.execute(
        script,
        Collections.singletonList(redisKey),
        param.getToken());

    if (ObjectUtil.isEmpty(result)) {
      log.error("Lua脚本执行异常，scene={}, userId={}", param.getScene(), param.getUserId());
      return SubmitTokenValidateResponse.builder()
          .valid(false)
          .message("Token验证失败，系统异常")
          .token(param.getToken())
          .build();
    }

    // 根据返回值判断结果
    if (result == 1L) {
      log.info("Token验证成功并已删除，scene={}, userId={}, token={}",
          param.getScene(), param.getUserId(), param.getToken());
      return SubmitTokenValidateResponse.builder()
          .valid(true)
          .message("验证通过")
          .token(param.getToken())
          .build();
    } else if (result == 0L) {
      log.warn("Token不存在或已过期，scene={}, userId={}, token={}",
          param.getScene(), param.getUserId(), param.getToken());
      return SubmitTokenValidateResponse.builder()
          .valid(false)
          .message("Token不存在或已过期，请重新获取")
          .token(param.getToken())
          .build();
    } else {
      log.warn("Token不匹配，scene={}, userId={}, token={}",
          param.getScene(), param.getUserId(), param.getToken());
      return SubmitTokenValidateResponse.builder()
          .valid(false)
          .message("Token无效")
          .token(param.getToken())
          .build();
    }
  }

  /**
   * 构建Token的Redis Key
   */
  private String buildTokenKey(String scene, String userId, String bizId) {
    StringBuilder keyBuilder = new StringBuilder(SubmitTokenConstant.getTokenPrefix())
        .append(scene)
        .append(":")
        .append(userId);

    if (bizId != null && !bizId.isEmpty()) {
      keyBuilder.append(":").append(bizId);
    }

    return keyBuilder.toString();
  }

  /**
   * 删除Token（用于特殊场景下手动清除）
   *
   * @param scene  业务场景
   * @param userId 用户ID
   * @param bizId  业务ID
   * @return 是否删除成功
   */
  public boolean deleteToken(String scene, String userId, String bizId) {
    String redisKey = buildTokenKey(scene, userId, bizId);
    Boolean result = redisTemplate.delete(redisKey);

    log.info("删除Token，scene={}, userId={}, bizId={}, result={}",
        scene, userId, bizId, result);

    return Boolean.TRUE.equals(result);
  }
}






