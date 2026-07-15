package com.blog.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 密码强度验证器
 * 验证密码强度要求
 * 需求: 11.1
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    
    private int minLength;
    private int maxLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    
    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
        this.maxLength = constraintAnnotation.maxLength();
        this.requireUppercase = constraintAnnotation.requireUppercase();
        this.requireLowercase = constraintAnnotation.requireLowercase();
        this.requireDigit = constraintAnnotation.requireDigit();
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null值由@NotNull或@NotBlank处理
        if (value == null) {
            return true;
        }
        
        // 检查长度
        if (value.length() < minLength || value.length() > maxLength) {
            return false;
        }
        
        // 检查是否包含大写字母
        if (requireUppercase && !value.matches(".*[A-Z].*")) {
            return false;
        }
        
        // 检查是否包含小写字母
        if (requireLowercase && !value.matches(".*[a-z].*")) {
            return false;
        }
        
        // 检查是否包含数字
        if (requireDigit && !value.matches(".*\\d.*")) {
            return false;
        }
        
        return true;
    }
}
