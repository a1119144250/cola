package com.xiaowang.cola.tools.controller;

import com.xiaowang.cola.tools.domain.resp.SubmitTokenResponse;
import com.xiaowang.cola.tools.domain.resp.SubmitTokenValidateResponse;
import com.xiaowang.cola.tools.domain.service.SubmitTokenService;
import com.xiaowang.cola.tools.param.SubmitTokenGenerateParam;
import com.xiaowang.cola.tools.param.SubmitTokenValidateParam;
import com.xiaowang.cola.web.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 防重Token控制器
 * 用于防止订单等业务的重复提交
 *
 * @author cola
 */
@Slf4j
@RestController
@RequestMapping("/submit-token")
@RequiredArgsConstructor
public class SubmitTokenController {

  private final SubmitTokenService submitTokenService;

  /**
   * 生成防重Token
   * 在用户进入提交页面时调用，获取Token
   *
   * @param param 生成参数
   * @return Token信息
   */
  @PostMapping("/generate")
  public Result<SubmitTokenResponse> generateToken(@Valid @RequestBody SubmitTokenGenerateParam param) {
    log.info("收到生成Token请求，scene={}, userId={}, bizId={}",
        param.getScene(), param.getUserId(), param.getBizId());

    SubmitTokenResponse response = submitTokenService.generateToken(param);
    return Result.success(response);
  }

  /**
   * 验证防重Token
   * 在用户提交订单等操作时调用，验证Token（验证成功后Token会被删除，确保一次性使用）
   *
   * @param param 验证参数
   * @return 验证结果
   */
  @PostMapping("/validate")
  public Result<SubmitTokenValidateResponse> validateToken(@Valid @RequestBody SubmitTokenValidateParam param) {
    log.info("收到验证Token请求，scene={}, userId={}, token={}",
        param.getScene(), param.getUserId(), param.getToken());

    SubmitTokenValidateResponse response = submitTokenService.validateToken(param);
    return Result.success(response);
  }

  /**
   * 删除Token（可选接口，用于特殊场景）
   *
   * @param scene  业务场景
   * @param userId 用户ID
   * @param bizId  业务ID（可选）
   * @return 删除结果
   */
  @DeleteMapping("/delete")
  public Result<Boolean> deleteToken(
      @RequestParam String scene,
      @RequestParam String userId,
      @RequestParam(required = false) String bizId) {

    log.info("收到删除Token请求，scene={}, userId={}, bizId={}", scene, userId, bizId);
    boolean result = submitTokenService.deleteToken(scene, userId, bizId);
    return Result.success(result);
  }
}






