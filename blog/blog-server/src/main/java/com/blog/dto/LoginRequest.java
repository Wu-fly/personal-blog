package com.blog.dto;

import com.blog.validation.Phone;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 用户登录请求DTO
 * 需求: 1.1-1.9
 */
@Data
public class LoginRequest {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Phone
    private String phone;

    /**
     * 短信验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String smsCode;
}
