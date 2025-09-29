package com.xiaowang.cola.user.domain.entity.convertor;

import com.xiaowang.cola.api.user.constant.UserStateEnum;
import com.xiaowang.cola.api.user.response.data.BasicUserInfo;
import com.xiaowang.cola.api.user.response.data.UserInfo;
import com.xiaowang.cola.user.domain.entity.User;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-09-29T15:54:35+0800",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.43.0.v20250819-1513, environment: Java 21.0.8 (Eclipse Adoptium)"
)
public class UserConvertorImpl implements UserConvertor {

    @Override
    public UserInfo mapToVo(User request) {
        if ( request == null ) {
            return null;
        }

        UserInfo userInfo = new UserInfo();

        if ( request.getId() != null ) {
            userInfo.setUserId( request.getId() );
        }
        if ( request.getGmtCreate() != null ) {
            userInfo.setCreateTime( request.getGmtCreate() );
        }
        if ( request.getNickName() != null ) {
            userInfo.setNickName( request.getNickName() );
        }
        if ( request.getProfilePhotoUrl() != null ) {
            userInfo.setProfilePhotoUrl( request.getProfilePhotoUrl() );
        }
        if ( request.getBlockChainPlatform() != null ) {
            userInfo.setBlockChainPlatform( request.getBlockChainPlatform() );
        }
        if ( request.getBlockChainUrl() != null ) {
            userInfo.setBlockChainUrl( request.getBlockChainUrl() );
        }
        if ( request.getCertification() != null ) {
            userInfo.setCertification( request.getCertification() );
        }
        if ( request.getInviteCode() != null ) {
            userInfo.setInviteCode( request.getInviteCode() );
        }
        if ( request.getState() != null ) {
            userInfo.setState( request.getState().name() );
        }
        if ( request.getTelephone() != null ) {
            userInfo.setTelephone( request.getTelephone() );
        }
        if ( request.getUserRole() != null ) {
            userInfo.setUserRole( request.getUserRole() );
        }

        return userInfo;
    }

    @Override
    public BasicUserInfo mapToBasicVo(User request) {
        if ( request == null ) {
            return null;
        }

        BasicUserInfo basicUserInfo = new BasicUserInfo();

        if ( request.getId() != null ) {
            basicUserInfo.setUserId( request.getId() );
        }
        if ( request.getNickName() != null ) {
            basicUserInfo.setNickName( request.getNickName() );
        }
        if ( request.getProfilePhotoUrl() != null ) {
            basicUserInfo.setProfilePhotoUrl( request.getProfilePhotoUrl() );
        }

        return basicUserInfo;
    }

    @Override
    public User mapToEntity(UserInfo request) {
        if ( request == null ) {
            return null;
        }

        User user = new User();

        if ( request.getUserId() != null ) {
            user.setId( request.getUserId() );
        }
        if ( request.getBlockChainPlatform() != null ) {
            user.setBlockChainPlatform( request.getBlockChainPlatform() );
        }
        if ( request.getBlockChainUrl() != null ) {
            user.setBlockChainUrl( request.getBlockChainUrl() );
        }
        if ( request.getCertification() != null ) {
            user.setCertification( request.getCertification() );
        }
        if ( request.getInviteCode() != null ) {
            user.setInviteCode( request.getInviteCode() );
        }
        if ( request.getNickName() != null ) {
            user.setNickName( request.getNickName() );
        }
        if ( request.getProfilePhotoUrl() != null ) {
            user.setProfilePhotoUrl( request.getProfilePhotoUrl() );
        }
        if ( request.getState() != null ) {
            user.setState( Enum.valueOf( UserStateEnum.class, request.getState() ) );
        }
        if ( request.getTelephone() != null ) {
            user.setTelephone( request.getTelephone() );
        }
        if ( request.getUserRole() != null ) {
            user.setUserRole( request.getUserRole() );
        }

        return user;
    }

    @Override
    public List<UserInfo> mapToVo(List<User> request) {
        if ( request == null ) {
            return null;
        }

        List<UserInfo> list = new ArrayList<UserInfo>( request.size() );
        for ( User user : request ) {
            list.add( mapToVo( user ) );
        }

        return list;
    }
}
