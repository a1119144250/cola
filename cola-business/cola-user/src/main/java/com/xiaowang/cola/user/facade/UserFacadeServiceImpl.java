package com.xiaowang.cola.user.facade;

import com.xiaowang.cola.api.user.request.UserActiveRequest;
import com.xiaowang.cola.api.user.request.UserAuthRequest;
import com.xiaowang.cola.api.user.request.UserModifyRequest;
import com.xiaowang.cola.api.user.request.UserPageQueryRequest;
import com.xiaowang.cola.api.user.request.UserQueryRequest;
import com.xiaowang.cola.api.user.request.UserRegisterRequest;
import com.xiaowang.cola.api.user.request.condition.UserIdQueryCondition;
import com.xiaowang.cola.api.user.request.condition.UserPhoneAndPasswordQueryCondition;
import com.xiaowang.cola.api.user.request.condition.UserPhoneQueryCondition;
import com.xiaowang.cola.api.user.request.condition.UserQueryCondition;
import com.xiaowang.cola.api.user.response.UserOperatorResponse;
import com.xiaowang.cola.api.user.response.UserQueryResponse;
import com.xiaowang.cola.api.user.response.data.UserInfo;
import com.xiaowang.cola.api.user.service.UserFacadeService;
import com.xiaowang.cola.base.response.PageResponse;
import com.xiaowang.cola.rpc.facade.Facade;
import com.xiaowang.cola.user.domain.entity.User;
import com.xiaowang.cola.user.domain.entity.convertor.UserConvertor;
import com.xiaowang.cola.user.domain.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author cola
 */
@DubboService(version = "1.0.0")
public class UserFacadeServiceImpl implements UserFacadeService {

    @Autowired
    private UserService userService;

    @Facade
    @Override
    public UserQueryResponse<UserInfo> query(UserQueryRequest userQueryRequest) {
        //使用switch表达式精简代码，如果这里编译不过，参考我的文档调整IDEA的JDK版本
        //文档地址：https://thoughts.aliyun.com/workspaces/6655879cf459b7001ba42f1b/docs/6673f26c5e11940001c810fb#667971268a5c151234adcf92

        UserQueryCondition userQueryCondition = userQueryRequest.getUserQueryCondition();
        User user = switch (userQueryCondition) {
            case UserIdQueryCondition userIdQueryCondition:
                yield userService.findById(userIdQueryCondition.getUserId());
            case UserPhoneQueryCondition userPhoneQueryCondition:
                yield userService.findByTelephone(userPhoneQueryCondition.getTelephone());
            case UserPhoneAndPasswordQueryCondition userPhoneAndPasswordQueryCondition:
                yield userService.findByTelephoneAndPass(userPhoneAndPasswordQueryCondition.getTelephone(), userPhoneAndPasswordQueryCondition.getPassword());
            default:
                throw new UnsupportedOperationException(userQueryRequest.getUserQueryCondition() + "'' is not supported");
        };

        UserQueryResponse<UserInfo> response = new UserQueryResponse();
        response.setSuccess(true);
        UserInfo userInfo = UserConvertor.INSTANCE.mapToVo(user);
        response.setData(userInfo);
        return response;
    }

    @Facade
    @Override
    public PageResponse<UserInfo> pageQuery(UserPageQueryRequest userPageQueryRequest) {
        var queryResult = userService.pageQueryByState(userPageQueryRequest.getKeyWord(), userPageQueryRequest.getState(), userPageQueryRequest.getCurrentPage(), userPageQueryRequest.getPageSize());
        PageResponse<UserInfo> response = new PageResponse<>();
        if (!queryResult.getSuccess()) {
            response.setSuccess(false);
            return response;
        }
        response.setSuccess(true);
        response.setDatas(UserConvertor.INSTANCE.mapToVo(queryResult.getDatas()));
        response.setCurrentPage(queryResult.getCurrentPage());
        response.setPageSize(queryResult.getPageSize());
        return response;
    }

    @Override
    @Facade
    public UserOperatorResponse register(UserRegisterRequest userRegisterRequest) {
        return userService.register(userRegisterRequest.getTelephone(), userRegisterRequest.getInviteCode());
    }

    @Override
    @Facade
    public UserOperatorResponse modify(UserModifyRequest userModifyRequest) {
        return userService.modify(userModifyRequest);
    }

    @Override
    @Facade
    public UserOperatorResponse auth(UserAuthRequest userAuthRequest) {
        return userService.auth(userAuthRequest);
    }

    @Override
    @Facade
    public UserOperatorResponse active(UserActiveRequest userActiveRequest) {
        return userService.active(userActiveRequest);
    }
}
