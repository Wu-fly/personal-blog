package com.blog.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * 手机号验证器
 * 验证中国大陆手机号格式
 * 需求: 11.1
 */
public class PhoneValidator implements ConstraintValidator<Phone, String> {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    @Override
    public void initialize(Phone constraintAnnotation) {
        // 初始化方法，可以获取注解参数
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null值由@NotNull或@NotBlank处理
        if (value == null) {
            return true;
        }
        
        return PHONE_PATTERN.matcher(value).matches();
    }
}
