package com.blog.dto;

import com.blog.validation.Phone;
import com.blog.validation.StrongPassword;
import com.blog.validation.ValidEmail;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 用户注册请求DTO
 * 需求: 15.1-15.8
 */
@Data
public class RegisterRequest {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Phone
    private String phone;

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @ValidEmail
    private String email;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @StrongPassword
    private String password;

    /**
     * 短信验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String smsCode;
}
