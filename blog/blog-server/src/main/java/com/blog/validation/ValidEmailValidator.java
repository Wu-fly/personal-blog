package com.blog.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * 邮箱验证器
 * 验证邮箱格式
 * 需求: 11.1
 */
public class ValidEmailValidator implements ConstraintValidator<ValidEmail, String> {
    
    // RFC 5322标准的简化版邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    @Override
    public void initialize(ValidEmail constraintAnnotation) {
        // 初始化方法，可以获取注解参数
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null值由@NotNull或@NotBlank处理
        if (value == null) {
            return true;
        }
        
        return EMAIL_PATTERN.matcher(value).matches();
    }
}
