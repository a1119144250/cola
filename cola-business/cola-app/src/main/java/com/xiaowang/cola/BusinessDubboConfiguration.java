package com.xiaowang.cola;

import com.xiaowang.cola.api.user.service.UserFacadeService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * dubbo配置
 *
 * @author cola
 */
@Configuration
public class BusinessDubboConfiguration {

    //@DubboReference(version = "1.0.0")
    //private ChainFacadeService chainFacadeService;

    //@Bean
    //@ConditionalOnMissingBean(name = "chainFacadeService")
    //public ChainFacadeService chainFacadeService() {
    //    return chainFacadeService;
    //}

    @DubboReference(version = "1.0.0")
    private UserFacadeService userFacadeService;

    @Bean
    @ConditionalOnMissingBean(name = "userFacadeService")
    public UserFacadeService userFacadeService() {
        return userFacadeService;
    }


}
