package com.blog.validation;

import com.blog.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证注解测试
 * 测试自定义验证注解和DTO验证
 * 需求: 11.1
 */
class ValidationAnnotationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ========== 手机号验证测试 ==========

    @Test
    void testPhoneValidation_ValidPhone() {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhone("13812345678");

        Set<ConstraintViolation<SendSmsRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "有效手机号应该通过验证");
    }

    @Test
    void testPhoneValidation_InvalidPhone_WrongFormat() {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhone("12345678901"); // 第二位不是3-9

        Set<ConstraintViolation<SendSmsRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "无效手机号应该验证失败");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("手机号格式不正确")));
    }

    @Test
    void testPhoneValidation_InvalidPhone_TooShort() {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhone("138123456"); // 少于11位

        Set<ConstraintViolation<SendSmsRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "手机号长度不足应该验证失败");
    }

    @Test
    void testPhoneValidation_InvalidPhone_TooLong() {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhone("138123456789"); // 超过11位

        Set<ConstraintViolation<SendSmsRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "手机号长度过长应该验证失败");
    }

    @Test
    void testPhoneValidation_EmptyPhone() {
        SendSmsRequest request = new SendSmsRequest();
        request.setPhone("");

        Set<ConstraintViolation<SendSmsRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "空手机号应该验证失败");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("手机号不能为空")));
    }

    // ========== 邮箱验证测试 ==========

    @Test
    void testEmailValidation_ValidEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13812345678");
        request.setEmail("test@example.com");
        request.setPassword("Test123");
        request.setSmsCode("123456");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "有效邮箱应该通过验证");
    }

    @Test
    void testEmailValidation_InvalidEmail_NoAtSign() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13812345678");
        request.setEmail("testexample.com"); // 缺少@
        request.setPassword("Test123");
        request.setSmsCode("123456");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "无效邮箱应该验证失败");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("邮箱格式不正确")));
    }

    @Test
    void testEmailValidation_InvalidEmail_NoDomain() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13812345678");
        request.setEmail("test@"); // 缺少域名
        request.setPassword("Test123");
        request.setSmsCode("123456");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "无效邮箱应该验证失败");
    }

    @Test
    void testEmailValidation_EmptyEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13812345678");
        request.setEmail("");
        request.setPassword("Test123");
        request.setSmsCode("123456");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "空邮箱应该验证失败");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("邮箱不能为空")));
    }

    // ========== 密码强度验证测试 ==========

    @Test
    void testPasswordValidation_ValidPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13812345678");
        request.setEmail("test@example.com");
        request.setPassword("Test123");
        request.setSmsCode("123456");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "有效密码应该通过验证");
    }

    @Test
    void testPasswordValidation_InvalidPassword_NoUppercase() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13812345678");
        request.setEmail("test@example.com");
        request.setPassword("test123"); // 缺少大写字母
        request.setSmsCode("123456");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "缺少大写字母的密码应该验证失败");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("密码必须包含大小写字母和数字")));
    }

    @Test
    void testPasswordValidation_InvalidPassword_NoLowercase() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13812345678");
        request.setEmail("test@example.com");
        request.setPassword("TEST123"); // 缺少小写字母
        request.setSmsCode("123456");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "缺少小写字母的密码应该验证失败");
    }

    @Test
    void testPasswordValidation_InvalidPassword_NoDigit() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13812345678");
        request.setEmail("test@example.com");
        request.setPassword("TestAbc"); // 缺少数字
        request.setSmsCode("123456");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "缺少数字的密码应该验证失败");
    }

    @Test
    void testPasswordValidation_InvalidPassword_TooShort() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13812345678");
        request.setEmail("test@example.com");
        request.setPassword("Te1"); // 少于6位
        request.setSmsCode("123456");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "密码长度不足应该验证失败");
    }

    @Test
    void testPasswordValidation_InvalidPassword_TooLong() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13812345678");
        request.setEmail("test@example.com");
        request.setPassword("Test123456789012345678"); // 超过20位
        request.setSmsCode("123456");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "密码长度过长应该验证失败");
    }

    @Test
    void testPasswordValidation_EmptyPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13812345678");
        request.setEmail("test@example.com");
        request.setPassword("");
        request.setSmsCode("123456");

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "空密码应该验证失败");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("密码不能为空")));
    }

    // ========== 文章验证测试 ==========

    @Test
    void testArticleValidation_EmptyTitle() {
        ArticleRequest request = new ArticleRequest();
        request.setTitle("");
        request.setContent("文章内容");

        Set<ConstraintViolation<ArticleRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "空标题应该验证失败");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("文章标题不能为空")));
    }

    @Test
    void testArticleValidation_EmptyContent() {
        ArticleRequest request = new ArticleRequest();
        request.setTitle("文章标题");
        request.setContent("");

        Set<ConstraintViolation<ArticleRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "空内容应该验证失败");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("文章内容不能为空")));
    }

    @Test
    void testArticleValidation_InvalidPrice() {
        ArticleRequest request = new ArticleRequest();
        request.setTitle("文章标题");
        request.setContent("文章内容");
        request.setIsPaid(true);
        request.setPrice(BigDecimal.ZERO); // 价格为0

        Set<ConstraintViolation<ArticleRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "价格为0应该验证失败");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("文章价格必须大于0")));
    }

    // ========== 评论验证测试 ==========

    @Test
    void testCommentValidation_EmptyContent() {
        CommentRequest request = new CommentRequest();
        request.setArticleId(1L);
        request.setContent("");

        Set<ConstraintViolation<CommentRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "空评论内容应该验证失败");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("评论内容不能为空")));
    }

    // ========== 私信验证测试 ==========

    @Test
    void testMessageValidation_EmptyContent() {
        MessageRequest request = new MessageRequest();
        request.setReceiverId(1L);
        request.setContent("");

        Set<ConstraintViolation<MessageRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "空私信内容应该验证失败");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("消息内容不能为空")));
    }

    // ========== 登录请求验证测试 ==========

    @Test
    void testLoginValidation_ValidRequest() {
        LoginRequest request = new LoginRequest();
        request.setPhone("13812345678");
        request.setSmsCode("123456");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "有效登录请求应该通过验证");
    }

    @Test
    void testLoginValidation_InvalidSmsCode() {
        LoginRequest request = new LoginRequest();
        request.setPhone("13812345678");
        request.setSmsCode("12345"); // 少于6位

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "无效验证码应该验证失败");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("验证码格式不正确")));
    }
}
