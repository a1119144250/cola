package com.xiaowang.cola.tools.domain.strategy;

/**
 * 支付策略
 * @author wangjin
 */
public interface PayStrategy {
    void pay(double amount);
}
