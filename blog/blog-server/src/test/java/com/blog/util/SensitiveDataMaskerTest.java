package com.blog.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 敏感数据脱敏工具类测试
 */
class SensitiveDataMaskerTest {
    
    @Test
    void testMaskPhone() {
        // 正常手机号
        assertEquals("138****5678", SensitiveDataMasker.maskPhone("13812345678"));
        
        // 短手机号
        assertEquals("123", SensitiveDataMasker.maskPhone("123"));
        
        // null
        assertNull(SensitiveDataMasker.maskPhone(null));
    }
    
    @Test
    void testMaskEmail() {
        // 正常邮箱
        assertEquals("te***@example.com", SensitiveDataMasker.maskEmail("test@example.com"));
        
        // 短邮箱
        assertEquals("a***@example.com", SensitiveDataMasker.maskEmail("a@example.com"));
        
        // 无@符号
        assertEquals("invalid", SensitiveDataMasker.maskEmail("invalid"));
        
        // null
        assertNull(SensitiveDataMasker.maskEmail(null));
    }
    
    @Test
    void testMaskIdCard() {
        // 正常身份证
        assertEquals("110101********1234", SensitiveDataMasker.maskIdCard("110101199001011234"));
        
        // 短身份证
        assertEquals("123", SensitiveDataMasker.maskIdCard("123"));
        
        // null
        assertNull(SensitiveDataMasker.maskIdCard(null));
    }
    
    @Test
    void testMaskBankCard() {
        // 正常银行卡
        assertEquals("************1234", SensitiveDataMasker.maskBankCard("6222021234567891234"));
        
        // 短银行卡
        assertEquals("123", SensitiveDataMasker.maskBankCard("123"));
        
        // null
        assertNull(SensitiveDataMasker.maskBankCard(null));
    }
    
    @Test
    void testMaskRealName() {
        // 正常姓名
        assertEquals("张**", SensitiveDataMasker.maskRealName("张三"));
        assertEquals("李**", SensitiveDataMasker.maskRealName("李四"));
        
        // 单字姓名
        assertEquals("王", SensitiveDataMasker.maskRealName("王"));
        
        // 空字符串
        assertEquals("", SensitiveDataMasker.maskRealName(""));
        
        // null
        assertNull(SensitiveDataMasker.maskRealName(null));
    }
    
    @Test
    void testMaskJson() {
        // 测试手机号脱敏
        String json1 = "{\"phone\":\"13812345678\"}";
        String masked1 = SensitiveDataMasker.mask(json1);
        assertTrue(masked1.contains("138****5678"));
        
        // 测试邮箱脱敏
        String json2 = "{\"email\":\"test@example.com\"}";
        String masked2 = SensitiveDataMasker.mask(json2);
        assertTrue(masked2.contains("te***@example.com"));
        
        // 测试密码脱敏
        String json3 = "{\"password\":\"mypassword123\"}";
        String masked3 = SensitiveDataMasker.mask(json3);
        assertTrue(masked3.contains("\"password\":\"******\""));
        
        // 测试验证码脱敏
        String json4 = "{\"code\":\"123456\"}";
        String masked4 = SensitiveDataMasker.mask(json4);
        assertTrue(masked4.contains("\"code\":\"****\""));
        
        // 测试Token脱敏
        String json5 = "{\"token\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9\"}";
        String masked5 = SensitiveDataMasker.mask(json5);
        assertTrue(masked5.contains("eyJhbGciOi..."));
        
        // 测试null
        assertNull(SensitiveDataMasker.mask(null));
        
        // 测试空字符串
        assertEquals("", SensitiveDataMasker.mask(""));
    }
    
    @Test
    void testMaskComplexJson() {
        // 测试复杂JSON
        String json = "{\"phone\":\"13812345678\",\"email\":\"test@example.com\",\"password\":\"secret123\",\"code\":\"123456\"}";
        String masked = SensitiveDataMasker.mask(json);
        
        assertTrue(masked.contains("138****5678"));
        assertTrue(masked.contains("te***@example.com"));
        assertTrue(masked.contains("\"password\":\"******\""));
        assertTrue(masked.contains("\"code\":\"****\""));
    }
}
