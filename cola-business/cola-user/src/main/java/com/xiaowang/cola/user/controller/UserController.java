package com.xiaowang.cola.user.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.xiaowang.cola.api.user.request.UserModifyRequest;
import com.xiaowang.cola.api.user.response.data.BasicUserInfo;
import com.xiaowang.cola.api.user.response.data.UserInfo;
import com.xiaowang.cola.user.domain.entity.User;
import com.xiaowang.cola.user.domain.entity.convertor.UserConvertor;
import com.xiaowang.cola.user.domain.service.UserService;
import com.xiaowang.cola.user.infrastructure.exception.UserException;
import com.xiaowang.cola.user.param.UserModifyParam;
import com.xiaowang.cola.web.vo.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.xiaowang.cola.user.infrastructure.exception.UserErrorCode.USER_NOT_EXIST;
import static com.xiaowang.cola.user.infrastructure.exception.UserErrorCode.USER_PASSWD_CHECK_FAIL;

/**
 * 用户信息
 *
 * @author cola
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("user")
public class UserController {

  @Autowired
  private UserService userService;

  @GetMapping("/getUserInfo")
  public Result<UserInfo> getUserInfo() {
    String userId = (String) StpUtil.getLoginId();
    User user = userService.findById(Long.valueOf(userId));

    if (user == null) {
      throw new UserException(USER_NOT_EXIST);
    }
    return Result.success(UserConvertor.INSTANCE.mapToVo(user));
  }

  @GetMapping("/queryUserByTel")
  public Result<BasicUserInfo> queryUserByTel(String telephone) {
    User user = userService.findByTelephone(telephone);
    if (user == null) {
      throw new UserException(USER_NOT_EXIST);
    }
    return Result.success(UserConvertor.INSTANCE.mapToBasicVo(user));
  }

  @PostMapping("/modifyNickName")
  public Result<Boolean> modifyNickName(@Valid @RequestBody UserModifyParam userModifyParam) {
    String userId = (String) StpUtil.getLoginId();

    // 修改信息
    UserModifyRequest userModifyRequest = new UserModifyRequest();
    userModifyRequest.setUserId(Long.valueOf(userId));
    userModifyRequest.setNickName(userModifyParam.getNickName());

    Boolean registerResult = userService.modify(userModifyRequest).getSuccess();
    return Result.success(registerResult);
  }

  @PostMapping("/modifyPassword")
  public Result<Boolean> modifyPassword(@Valid @RequestBody UserModifyParam userModifyParam) {
    // 查询用户信息
    String userId = (String) StpUtil.getLoginId();
    User user = userService.findById(Long.valueOf(userId));

    if (user == null) {
      throw new UserException(USER_NOT_EXIST);
    }
    if (!StringUtils.equals(user.getPasswordHash(), DigestUtil.md5Hex(userModifyParam.getOldPassword()))) {
      throw new UserException(USER_PASSWD_CHECK_FAIL);
    }
    // 修改信息
    UserModifyRequest userModifyRequest = new UserModifyRequest();
    userModifyRequest.setUserId(Long.valueOf(userId));
    userModifyRequest.setPassword(userModifyParam.getNewPassword());
    Boolean registerResult = userService.modify(userModifyRequest).getSuccess();
    return Result.success(registerResult);
  }
}
