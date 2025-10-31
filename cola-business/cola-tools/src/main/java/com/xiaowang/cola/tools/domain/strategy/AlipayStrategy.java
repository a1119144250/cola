package com.xiaowang.cola.tools.domain.service;

/**
 * 支付宝支付策略
 * @author: wangjin
 **/
public class AlipayStrategy implements PayStrategy  {

    @Override
    public void pay(double amount) {
        System.out.println("支付宝支付" + amount);
    }
}
