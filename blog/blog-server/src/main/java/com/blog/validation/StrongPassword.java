package com.blog.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 密码强度验证注解
 * 验证密码必须包含大小写字母和数字，长度6-20位
 * 需求: 11.1
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
@Documented
public @interface StrongPassword {
    
    String message() default "密码必须包含大小写字母和数字，长度6-20位";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 最小长度
     */
    int minLength() default 6;
    
    /**
     * 最大长度
     */
    int maxLength() default 20;
    
    /**
     * 是否要求包含大写字母
     */
    boolean requireUppercase() default true;
    
    /**
     * 是否要求包含小写字母
     */
    boolean requireLowercase() default true;
    
    /**
     * 是否要求包含数字
     */
    boolean requireDigit() default true;
}
