# 认证控制器实现文档

## 概述

AuthController 是个人博客系统的认证控制器，负责处理用户注册、登录、短信验证码发送、登出和令牌刷新等功能。

## API 接口

### 1. 用户注册

**端点**: `POST /api/auth/register`

**请求体**:
```json
{
  "phone": "13800138000",
  "email": "user@example.com",
  "password": "Test123456",
  "smsCode": "123456"
}
```

**响应** (201 Created):
```json
{
  "success": true,
  "message": "注册成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "role": "USER",
    "nickname": "用户昵称",
    "avatar": "https://example.com/avatar.jpg"
  },
  "timestamp": "2025-12-25T10:30:00"
}
```

**验证规则**:
- 手机号: 必填，格式为 `1[3-9]\d{9}`
- 邮箱: 必填，符合邮箱格式
- 密码: 必填，6-20位，必须包含大小写字母和数字
- 验证码: 必填，6位数字

**需求**: 15.1-15.8

---

### 2. 发送短信验证码

**端点**: `POST /api/auth/send-sms`

**请求体**:
```json
{
  "phone": "13800138000"
}
```

**响应** (200 OK):
```json
{
  "success": true,
  "message": "验证码发送成功",
  "data": {
    "message": "验证码已发送",
    "smsCode": "123456"  // 仅测试环境返回
  },
  "timestamp": "2025-12-25T10:30:00"
}
```

**验证规则**:
- 手机号: 必填，格式为 `1[3-9]\d{9}`

**需求**: 1.2

---

### 3. 用户登录

**端点**: `POST /api/auth/login`

**请求体**:
```json
{
  "phone": "13800138000",
  "smsCode": "123456"
}
```

**响应** (200 OK):
```json
{
  "success": true,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "userId": 1,
    "role": "BLOGGER",
    "nickname": "博主昵称",
    "avatar": "https://example.com/avatar.jpg"
  },
  "timestamp": "2025-12-25T10:30:00"
}
```

**验证规则**:
- 手机号: 必填，格式为 `1[3-9]\d{9}`
- 验证码: 必填，6位数字

**需求**: 1.1-1.9

---

### 4. 用户登出

**端点**: `POST /api/auth/logout`

**请求头**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应** (200 OK):
```json
{
  "success": true,
  "message": "登出成功",
  "data": null,
  "timestamp": "2025-12-25T10:30:00"
}
```

**说明**:
- 由于使用JWT无状态认证，登出主要由前端处理（删除本地令牌）
- 后端可选实现将令牌加入Redis黑名单
- Authorization头为可选参数

**需求**: 1.1-1.9

---

### 5. 刷新令牌

**端点**: `POST /api/auth/refresh`

**请求头**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**响应** (200 OK):
```json
{
  "success": true,
  "message": "令牌刷新成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer"
  },
  "timestamp": "2025-12-25T10:30:00"
}
```

**验证规则**:
- Authorization头: 必填，格式为 `Bearer <token>`

**需求**: 1.9

---

## 错误处理

所有接口遵循统一的错误响应格式：

```json
{
  "success": false,
  "message": "错误描述",
  "data": null,
  "errorCode": "ERROR_CODE",
  "timestamp": "2025-12-25T10:30:00"
}
```

### 常见错误码

| HTTP状态码 | 错误场景 | 示例消息 |
|-----------|---------|---------|
| 400 | 请求参数验证失败 | "手机号格式不正确" |
| 401 | 认证失败 | "验证码错误" |
| 409 | 资源冲突 | "手机号已被注册" |
| 422 | 业务逻辑错误 | "账号不存在" |
| 500 | 服务器内部错误 | "系统错误，请稍后重试" |

---

## 参数验证

### 手机号验证
- 正则表达式: `^1[3-9]\d{9}$`
- 示例: `13800138000`, `15912345678`

### 邮箱验证
- 使用标准邮箱格式验证
- 示例: `user@example.com`

### 密码验证
- 长度: 6-20位
- 正则表达式: `^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{6,20}$`
- 必须包含: 大写字母、小写字母、数字
- 示例: `Test123456`, `Password1`

### 验证码验证
- 正则表达式: `^\d{6}$`
- 示例: `123456`, `987654`

---

## 日志记录

所有接口都记录了详细的日志信息：

### 成功日志
```
INFO  - 用户注册请求: phone=13800138000, email=test@example.com
INFO  - 用户注册成功: userId=1, role=USER
```

### 错误日志
```
ERROR - 用户注册失败: phone=13800138000, error=手机号已被注册
```

---

## 安全考虑

### 1. 输入验证
- 所有输入参数都经过严格的格式验证
- 使用 `@Valid` 注解自动触发验证
- 验证失败返回 400 Bad Request

### 2. 密码安全
- 密码在Service层使用BCrypt加密
- 密码不在日志中记录
- 密码必须满足强度要求

### 3. 令牌安全
- JWT令牌有效期为7天
- 令牌包含用户ID和角色信息
- 支持令牌刷新机制

### 4. 短信验证码
- 验证码有效期为5分钟
- 验证码使用后立即失效
- 生产环境不返回验证码内容

---

## 测试

### 单元测试

测试文件: `AuthControllerTest.java`

**测试覆盖**:
- ✅ 注册成功
- ✅ 注册失败（无效手机号）
- ✅ 注册失败（无效邮箱）
- ✅ 注册失败（弱密码）
- ✅ 发送短信成功
- ✅ 发送短信失败（无效手机号）
- ✅ 登录成功
- ✅ 登录失败（无效手机号）
- ✅ 登录失败（无效验证码）
- ✅ 登出成功（带令牌）
- ✅ 登出成功（不带令牌）
- ✅ 刷新令牌成功
- ✅ 刷新令牌失败（缺少Authorization头）

### 运行测试

```bash
mvn test -Dtest=AuthControllerTest
```

---

## 使用示例

### 前端调用示例 (JavaScript/Axios)

```javascript
// 1. 发送短信验证码
async function sendSmsCode(phone) {
  const response = await axios.post('/api/auth/send-sms', { phone });
  return response.data;
}

// 2. 用户注册
async function register(phone, email, password, smsCode) {
  const response = await axios.post('/api/auth/register', {
    phone,
    email,
    password,
    smsCode
  });
  
  // 保存令牌到localStorage
  localStorage.setItem('accessToken', response.data.data.accessToken);
  localStorage.setItem('userId', response.data.data.userId);
  
  return response.data;
}

// 3. 用户登录
async function login(phone, smsCode) {
  const response = await axios.post('/api/auth/login', {
    phone,
    smsCode
  });
  
  // 保存令牌到localStorage
  localStorage.setItem('accessToken', response.data.data.accessToken);
  localStorage.setItem('userId', response.data.data.userId);
  
  return response.data;
}

// 4. 用户登出
async function logout() {
  const token = localStorage.getItem('accessToken');
  
  await axios.post('/api/auth/logout', {}, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  
  // 清除本地令牌
  localStorage.removeItem('accessToken');
  localStorage.removeItem('userId');
}

// 5. 刷新令牌
async function refreshToken() {
  const oldToken = localStorage.getItem('accessToken');
  
  const response = await axios.post('/api/auth/refresh', {}, {
    headers: {
      'Authorization': `Bearer ${oldToken}`
    }
  });
  
  // 更新令牌
  localStorage.setItem('accessToken', response.data.data.accessToken);
  
  return response.data;
}
```

---

## 依赖关系

```
AuthController
    ↓
AuthService (接口)
    ↓
AuthServiceImpl (实现)
    ↓
├── UserRepository
├── WalletRepository
├── JwtUtil
└── SmsService (阿里云短信)
```

---

## 后续优化建议

### 1. 令牌黑名单
- 实现Redis黑名单机制
- 登出时将令牌加入黑名单
- 请求时检查令牌是否在黑名单中

### 2. 限流保护
- 对发送短信接口进行限流（每小时3次）
- 对登录接口进行限流（每分钟5次）
- 使用Redis实现分布式限流

### 3. 验证码增强
- 添加图形验证码防止机器人
- 实现滑动验证
- 记录验证码发送次数

### 4. 审计日志
- 记录所有认证相关操作
- 包含IP地址、设备信息
- 用于安全审计和异常检测

---

## 相关文档

- [AuthService实现文档](./AUTH_SERVICE_IMPLEMENTATION.md)
- [安全配置文档](./SECURITY_CONFIGURATION.md)
- [数据库配置文档](./DATABASE_CONFIGURATION.md)

---

## 更新日志

| 日期 | 版本 | 说明 |
|-----|------|------|
| 2025-12-25 | 1.0.0 | 初始版本，实现所有认证接口 |
