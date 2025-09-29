package com.xiaowang.cola.sms;


import com.xiaowang.cola.sms.response.SmsSendResponse;

/**
 * 短信服务
 *
 * @author cola
 */
public interface SmsService {
    /**
     * 发送短信
     *
     * @param phoneNumber
     * @param code
     * @return
     */
    public SmsSendResponse sendMsg(String phoneNumber, String code);
}
