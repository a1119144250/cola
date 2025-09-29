package com.xiaowang.cola.user.facade;

import com.xiaowang.cola.api.user.request.UserRegisterRequest;
import com.xiaowang.cola.api.user.response.UserOperatorResponse;
import com.xiaowang.cola.api.user.service.UserManageFacadeService;
import com.xiaowang.cola.rpc.facade.Facade;
import com.xiaowang.cola.user.domain.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author cola
 */
@DubboService(version = "1.0.0")
public class UserManageFacadeServiceImpl implements UserManageFacadeService {

    @Autowired
    private UserService userService;

    @Override
    @Facade
    public UserOperatorResponse registerAdmin(UserRegisterRequest userRegisterRequest) {
        return userService.registerAdmin(userRegisterRequest.getTelephone(), userRegisterRequest.getPassword());
    }

    @Override
    public UserOperatorResponse freeze(Long userId) {
        return userService.freeze(userId);
    }

    @Override
    public UserOperatorResponse unfreeze(Long userId) {
        return userService.unfreeze(userId);
    }
}
