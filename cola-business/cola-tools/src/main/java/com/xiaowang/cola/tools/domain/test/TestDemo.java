package com.xiaowang.cola.tools.domain.test;

import com.xiaowang.cola.tools.domain.entity.User;
import com.xiaowang.cola.tools.domain.enums.PayType;
import com.xiaowang.cola.tools.domain.factory.PayStrategyFactory;
import com.xiaowang.cola.tools.domain.strategy.PayStrategy;

/**
 * @author wangjin
 */
public class TestDemo {

    public static void main(String[] args) {


    }

    public static void main1(String[] args) {

        double amount = 100.0;

        // 用户选择支付宝支付
        PayType alipay = PayType.ALIPAY;
        PayStrategy payStrategy = PayStrategyFactory.getPayStrategy(alipay);
        payStrategy.pay(amount);

        System.out.println("-----------------");

        PayType wechat = PayType.WECHAT;
        payStrategy = PayStrategyFactory.getPayStrategy(wechat);
        payStrategy.pay(amount);

        System.out.println("-----------------");

        PayType bank = PayType.BANK;
        payStrategy = PayStrategyFactory.getPayStrategy(bank);
        payStrategy.pay(amount);

    }

}
