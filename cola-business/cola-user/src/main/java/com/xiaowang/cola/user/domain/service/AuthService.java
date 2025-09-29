package com.xiaowang.cola.user.domain.service;

/**
 * 认证服务
 *
 * @author cola
 */
public interface AuthService {
    /**
     * 校验认证信息
     *
     * @param realName
     * @param idCard
     * @return
     */
    public boolean checkAuth(String realName, String idCard);
}
