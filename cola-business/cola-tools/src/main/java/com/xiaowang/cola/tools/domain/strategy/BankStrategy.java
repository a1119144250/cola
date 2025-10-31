package com.xiaowang.cola.tools.domain.service;

/**
 * 银行卡支付策略
 * @author: wangjin
 **/
public class BankStrategy implements PayStrategy {

    @Override
    public void pay(double amount) {
        System.out.println("银行卡支付" + amount);
    }
}
