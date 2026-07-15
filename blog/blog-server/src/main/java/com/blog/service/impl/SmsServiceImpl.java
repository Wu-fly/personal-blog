package com.blog.service.impl;

import com.blog.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务实现类
 * 需求: 1.2 - 发送短信验证码到手机号
 * 需求: 1.3 - 验证短信验证码
 * 
 * 注意: 当前实现为模拟实现，生产环境需要集成阿里云短信服务
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final int CODE_LENGTH = 6;
    private static final long CODE_EXPIRATION = 5; // 5分钟过期

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 发送短信验证码
     * 
     * @param phone 手机号
     * @return 验证码（测试环境返回，生产环境不返回）
     */
    @Override
    public String sendSmsCode(String phone) {
        // 生成6位随机验证码
        String code = generateCode();
        
        // 存储到Redis，5分钟过期
        String key = SMS_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRATION, TimeUnit.MINUTES);
        
        log.info("SMS code sent to phone: {}, code: {} (test mode)", phone, code);
        
        // TODO: 生产环境需要集成阿里云短信服务
        // AliyunSmsClient.sendSms(phone, code);
        
        // 测试环境返回验证码，生产环境不应返回
        return code;
    }

    /**
     * 验证短信验证码
     * 
     * @param phone 手机号
     * @param code 验证码
     * @return true表示验证通过，false表示验证失败
     */
    @Override
    public boolean verifySmsCode(String phone, String code) {
        String key = SMS_CODE_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(key);
        
        if (storedCode == null) {
            log.warn("SMS code not found or expired for phone: {}", phone);
            return false;
        }
        
        boolean isValid = storedCode.equals(code);
        
        if (isValid) {
            // 验证成功后删除验证码
            redisTemplate.delete(key);
            log.info("SMS code verified successfully for phone: {}", phone);
        } else {
            log.warn("SMS code verification failed for phone: {}", phone);
        }
        
        return isValid;
    }

    /**
     * 生成随机验证码
     * 
     * @return 6位数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
