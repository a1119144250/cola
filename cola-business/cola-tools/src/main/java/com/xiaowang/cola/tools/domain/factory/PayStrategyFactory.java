package com.xiaowang.cola.tools.domain.factory;

import com.xiaowang.cola.tools.domain.enums.PayType;
import com.xiaowang.cola.tools.domain.strategy.AlipayStrategy;
import com.xiaowang.cola.tools.domain.strategy.BankStrategy;
import com.xiaowang.cola.tools.domain.strategy.PayStrategy;
import com.xiaowang.cola.tools.domain.strategy.WechatStrategy;

/**
 * 支付策略工厂
 * @author: wangjin
 **/
public class PayStrategyFactory {

    public static PayStrategy getPayStrategy(PayType payType) {
        return switch (payType) {
            case ALIPAY -> new AlipayStrategy();
            case WECHAT -> new WechatStrategy();
            case BANK -> new BankStrategy();
            default -> throw new IllegalArgumentException("未知支付方式");
        };
    }

}
