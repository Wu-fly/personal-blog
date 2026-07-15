package com.blog.service;

/**
 * 短信服务接口
 * 需求: 1.2 - 发送短信验证码
 */
public interface SmsService {

    /**
     * 发送短信验证码
     * 
     * @param phone 手机号
     * @return 验证码（用于测试环境，生产环境不返回）
     */
    String sendSmsCode(String phone);

    /**
     * 验证短信验证码
     * 
     * @param phone 手机号
     * @param code 验证码
     * @return true表示验证通过，false表示验证失败
     */
    boolean verifySmsCode(String phone, String code);
}
