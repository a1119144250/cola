package com.xiaowang.cola.api.user.service;


import com.xiaowang.cola.api.user.request.UserRegisterRequest;
import com.xiaowang.cola.api.user.response.UserOperatorResponse;

/**
 * @author cola
 */
public interface UserManageFacadeService {

    /**
     * 管理用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    UserOperatorResponse registerAdmin(UserRegisterRequest userRegisterRequest);

    /**
     * 用户冻结
     *
     * @param userId
     * @return
     */
    UserOperatorResponse freeze(Long userId);

    /**
     * 用户解冻
     *
     * @param userId
     * @return
     */
    UserOperatorResponse unfreeze(Long userId);

}
