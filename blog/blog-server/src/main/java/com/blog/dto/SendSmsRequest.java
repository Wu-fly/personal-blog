package com.blog.dto;

import com.blog.validation.Phone;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 发送短信验证码请求DTO
 * 需求: 1.2
 */
@Data
public class SendSmsRequest {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Phone
    private String phone;
}
