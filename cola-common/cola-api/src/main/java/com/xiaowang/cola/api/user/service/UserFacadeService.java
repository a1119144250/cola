package com.xiaowang.cola.api.user.service;


import com.xiaowang.cola.api.user.request.UserActiveRequest;
import com.xiaowang.cola.api.user.request.UserAuthRequest;
import com.xiaowang.cola.api.user.request.UserModifyRequest;
import com.xiaowang.cola.api.user.request.UserPageQueryRequest;
import com.xiaowang.cola.api.user.request.UserQueryRequest;
import com.xiaowang.cola.api.user.request.UserRegisterRequest;
import com.xiaowang.cola.api.user.response.UserOperatorResponse;
import com.xiaowang.cola.api.user.response.UserQueryResponse;
import com.xiaowang.cola.api.user.response.data.UserInfo;
import com.xiaowang.cola.base.response.PageResponse;

/**
 * @author cola
 */
public interface UserFacadeService {

    /**
     * 用户信息查询
     * @param userQueryRequest
     * @return
     */
    UserQueryResponse<UserInfo> query(UserQueryRequest userQueryRequest);


    /**
     * 分页查询用户信息
     * @param userPageQueryRequest
     * @return
     */
    PageResponse<UserInfo> pageQuery(UserPageQueryRequest userPageQueryRequest);

    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    UserOperatorResponse register(UserRegisterRequest userRegisterRequest);

    /**
     * 用户信息修改
     * @param userModifyRequest
     * @return
     */
    UserOperatorResponse modify(UserModifyRequest userModifyRequest);

    /**
     * 用户实名认证
     * @param userAuthRequest
     * @return
     */
    UserOperatorResponse auth(UserAuthRequest userAuthRequest);

    /**
     * 用户激活
     * @param userActiveRequest
     * @return
     */
    UserOperatorResponse active(UserActiveRequest userActiveRequest);

}
