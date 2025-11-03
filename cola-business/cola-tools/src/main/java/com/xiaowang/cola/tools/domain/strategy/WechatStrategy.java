package com.xiaowang.cola.tools.domain.strategy;

/**
 * 微信支付策略
 * @author: wangjin
 **/
public class WechatStrategy implements PayStrategy {
    @Override
    public void pay(double amount) {
        System.out.println("微信支付" + amount);
    }
}
