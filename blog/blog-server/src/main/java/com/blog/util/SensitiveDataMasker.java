package com.blog.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 敏感数据脱敏工具类
 * 用于日志记录时对敏感信息进行脱敏处理
 */
public class SensitiveDataMasker {
    
    // 手机号脱敏：保留前3位和后4位
    private static final Pattern PHONE_PATTERN = Pattern.compile("\"phone\"\\s*:\\s*\"(\\d{3})\\d{4}(\\d{4})\"");
    
    // 邮箱脱敏：保留前2位和@后的域名
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\"email\"\\s*:\\s*\"(\\w{1,2})\\w+(@\\w+\\.\\w+)\"");
    
    // 密码脱敏：完全隐藏
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("\"password\"\\s*:\\s*\"[^\"]+\"");
    
    // 身份证脱敏：保留前6位和后4位
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("\"idCard\"\\s*:\\s*\"(\\d{6})\\d{8}(\\d{4})\"");
    
    // 银行卡脱敏：保留后4位
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("\"bankCard\"\\s*:\\s*\"\\d+(\\d{4})\"");
    
    // 真实姓名脱敏：保留姓氏
    private static final Pattern NAME_PATTERN = Pattern.compile("\"realName\"\\s*:\\s*\"(.)(.+)\"");
    
    // Token脱敏：只显示前10位
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\"\\s*:\\s*\"(.{10})[^\"]+\"");
    
    // 验证码脱敏：完全隐藏
    private static final Pattern CODE_PATTERN = Pattern.compile("\"(code|verifyCode|smsCode)\"\\s*:\\s*\"[^\"]+\"");
    
    /**
     * 对JSON字符串中的敏感数据进行脱敏
     */
    public static String mask(String json) {
        if (json == null || json.isEmpty()) {
            return json;
        }
        
        String masked = json;
        
        // 手机号脱敏
        Matcher phoneMatcher = PHONE_PATTERN.matcher(masked);
        masked = phoneMatcher.replaceAll("\"phone\":\"$1****$2\"");
        
        // 邮箱脱敏
        Matcher emailMatcher = EMAIL_PATTERN.matcher(masked);
        masked = emailMatcher.replaceAll("\"email\":\"$1***$2\"");
        
        // 密码脱敏
        Matcher passwordMatcher = PASSWORD_PATTERN.matcher(masked);
        masked = passwordMatcher.replaceAll("\"password\":\"******\"");
        
        // 身份证脱敏
        Matcher idCardMatcher = ID_CARD_PATTERN.matcher(masked);
        masked = idCardMatcher.replaceAll("\"idCard\":\"$1********$2\"");
        
        // 银行卡脱敏
        Matcher bankCardMatcher = BANK_CARD_PATTERN.matcher(masked);
        masked = bankCardMatcher.replaceAll("\"bankCard\":\"************$1\"");
        
        // 真实姓名脱敏
        Matcher nameMatcher = NAME_PATTERN.matcher(masked);
        masked = nameMatcher.replaceAll("\"realName\":\"$1**\"");
        
        // Token脱敏
        Matcher tokenMatcher = TOKEN_PATTERN.matcher(masked);
        masked = tokenMatcher.replaceAll("\"token\":\"$1...\"");
        
        // 验证码脱敏
        Matcher codeMatcher = CODE_PATTERN.matcher(masked);
        masked = codeMatcher.replaceAll("\"$1\":\"****\"");
        
        return masked;
    }
    
    /**
     * 脱敏手机号
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    
    /**
     * 脱敏邮箱
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        if (parts[0].length() <= 2) {
            return email.charAt(0) + "***@" + parts[1];
        }
        return parts[0].substring(0, 2) + "***@" + parts[1];
    }
    
    /**
     * 脱敏身份证
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 18) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }
    
    /**
     * 脱敏银行卡
     */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 4) {
            return bankCard;
        }
        return "************" + bankCard.substring(bankCard.length() - 4);
    }
    
    /**
     * 脱敏真实姓名
     */
    public static String maskRealName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return name;
        }
        return name.charAt(0) + "**";
    }
}
