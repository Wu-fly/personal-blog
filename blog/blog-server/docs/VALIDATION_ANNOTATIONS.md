# 输入验证注解文档

## 概述

本文档描述了博客系统中使用的自定义验证注解，用于确保用户输入的数据符合业务规则和安全要求。

**需求**: 11.1 - 输入验证与安全

## 自定义验证注解

### 1. @Phone - 手机号验证

**位置**: `com.blog.validation.Phone`

**功能**: 验证中国大陆手机号格式

**规则**:
- 必须以1开头
- 第二位必须是3-9之间的数字
- 总共11位数字

**正则表达式**: `^1[3-9]\d{9}$`

**使用示例**:
```java
@NotBlank(message = "手机号不能为空")
@Phone
private String phone;
```

**有效示例**:
- 13812345678
- 15912345678
- 18812345678

**无效示例**:
- 12345678901 (第二位不是3-9)
- 138123456 (少于11位)
- 138123456789 (超过11位)

---

### 2. @ValidEmail - 邮箱验证

**位置**: `com.blog.validation.ValidEmail`

**功能**: 验证邮箱地址格式

**规则**:
- 符合RFC 5322标准的简化版邮箱格式
- 包含@符号
- 包含有效的域名
- 顶级域名长度2-7位

**正则表达式**: `^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$`

**使用示例**:
```java
@NotBlank(message = "邮箱不能为空")
@ValidEmail
private String email;
```

**有效示例**:
- test@example.com
- user.name@company.co.uk
- admin+tag@domain.org

**无效示例**:
- testexample.com (缺少@)
- test@ (缺少域名)
- test@domain (缺少顶级域名)

---

### 3. @StrongPassword - 密码强度验证

**位置**: `com.blog.validation.StrongPassword`

**功能**: 验证密码强度，确保密码安全性

**规则**:
- 长度: 6-20位 (可配置)
- 必须包含大写字母 (可配置)
- 必须包含小写字母 (可配置)
- 必须包含数字 (可配置)

**使用示例**:
```java
@NotBlank(message = "密码不能为空")
@StrongPassword
private String password;
```

**自定义配置示例**:
```java
@StrongPassword(
    minLength = 8,
    maxLength = 30,
    requireUppercase = true,
    requireLowercase = true,
    requireDigit = true
)
private String password;
```

**有效示例**:
- Test123
- Password1
- MyPass99

**无效示例**:
- test123 (缺少大写字母)
- TEST123 (缺少小写字母)
- TestAbc (缺少数字)
- Te1 (长度不足)

---

## DTO类中的应用

### RegisterRequest (用户注册)

```java
@Data
public class RegisterRequest {
    @NotBlank(message = "手机号不能为空")
    @Phone
    private String phone;

    @NotBlank(message = "邮箱不能为空")
    @ValidEmail
    private String email;

    @NotBlank(message = "密码不能为空")
    @StrongPassword
    private String password;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String smsCode;
}
```

### LoginRequest (用户登录)

```java
@Data
public class LoginRequest {
    @NotBlank(message = "手机号不能为空")
    @Phone
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String smsCode;
}
```

### SendSmsRequest (发送短信验证码)

```java
@Data
public class SendSmsRequest {
    @NotBlank(message = "手机号不能为空")
    @Phone
    private String phone;
}
```

### ArticleRequest (文章创建/更新)

```java
@Data
public class ArticleRequest {
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题不能超过200个字符")
    private String title;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    @DecimalMin(value = "0.0", inclusive = false, message = "文章价格必须大于0")
    private BigDecimal price;
}
```

### CommentRequest (评论)

```java
@Data
public class CommentRequest {
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    @NotBlank(message = "评论内容不能为空")
    private String content;
}
```

### MessageRequest (私信)

```java
@Data
public class MessageRequest {
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    @NotBlank(message = "消息内容不能为空")
    private String content;
}
```

---

## 验证流程

### 1. Controller层自动验证

在Controller方法参数上使用`@Valid`或`@Validated`注解：

```java
@PostMapping("/register")
public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    // 如果验证失败，会自动抛出MethodArgumentNotValidException
    return authService.register(request);
}
```

### 2. 全局异常处理

`GlobalExceptionHandler`会捕获验证异常并返回统一格式的错误响应：

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiResponse<Void>> handleValidationException(
        MethodArgumentNotValidException ex) {
    
    String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
    
    return ResponseEntity.badRequest()
            .body(ApiResponse.error(400, errorMessage));
}
```

### 3. 错误响应格式

```json
{
  "success": false,
  "code": 400,
  "message": "手机号格式不正确, 密码必须包含大小写字母和数字",
  "data": null
}
```

---

## 测试

### 单元测试

位置: `src/test/java/com/blog/validation/ValidationAnnotationTest.java`

测试覆盖:
- ✅ 手机号验证 (有效/无效格式、长度)
- ✅ 邮箱验证 (有效/无效格式)
- ✅ 密码强度验证 (大小写、数字、长度)
- ✅ 文章验证 (空标题、空内容、无效价格)
- ✅ 评论验证 (空内容)
- ✅ 私信验证 (空内容)

### 运行测试

```bash
mvn test -Dtest=ValidationAnnotationTest
```

---

## 最佳实践

### 1. 组合使用验证注解

```java
@NotBlank(message = "手机号不能为空")  // 先检查非空
@Phone                                  // 再检查格式
private String phone;
```

### 2. 自定义错误消息

```java
@Phone(message = "请输入正确的手机号码")
private String phone;
```

### 3. 分组验证

```java
public interface CreateGroup {}
public interface UpdateGroup {}

@NotNull(groups = CreateGroup.class)
@Phone
private String phone;
```

### 4. 嵌套对象验证

```java
@Valid  // 触发嵌套对象的验证
private AddressDTO address;
```

---

## 安全考虑

### 1. 防止注入攻击

所有用户输入都经过验证，确保格式正确，防止SQL注入和XSS攻击。

### 2. 密码强度要求

强制要求密码包含大小写字母和数字，提高账号安全性。

### 3. 输入长度限制

使用`@Size`注解限制输入长度，防止缓冲区溢出和DoS攻击。

### 4. 格式验证

使用正则表达式严格验证输入格式，拒绝不符合规范的数据。

---

## 扩展

### 添加新的验证注解

1. 创建注解接口:
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CustomValidator.class)
public @interface CustomValidation {
    String message() default "验证失败";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

2. 实现验证器:
```java
public class CustomValidator implements ConstraintValidator<CustomValidation, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 验证逻辑
        return true;
    }
}
```

3. 应用到DTO:
```java
@CustomValidation
private String field;
```

---

## 总结

本系统实现了完整的输入验证机制，包括:

1. ✅ 自定义验证注解 (@Phone, @ValidEmail, @StrongPassword)
2. ✅ 验证器实现 (PhoneValidator, ValidEmailValidator, StrongPasswordValidator)
3. ✅ DTO类应用验证注解
4. ✅ Controller层自动验证
5. ✅ 全局异常处理
6. ✅ 完整的单元测试

这些验证机制确保了系统的数据完整性和安全性，满足需求11.1的要求。
