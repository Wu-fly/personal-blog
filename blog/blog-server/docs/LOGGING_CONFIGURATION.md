# 日志系统配置文档

## 概述

本系统实现了完整的日志记录功能，包括API请求日志、错误日志、安全事件日志，并对敏感信息进行脱敏处理。

## 日志级别

系统使用标准的日志级别：

- **ERROR**: 系统错误、数据库错误、外部服务错误
- **WARN**: 业务逻辑错误、安全警告（如SQL注入尝试）
- **INFO**: 重要业务操作（如用户注册、文章发布、交易记录）
- **DEBUG**: 详细的调试信息
- **TRACE**: 最详细的跟踪信息（如SQL参数绑定）

## 日志文件

系统会生成以下日志文件（位于 `logs/` 目录）：

### 1. blog-server.log
**用途**: 记录所有日志信息

**内容**:
- 应用启动和关闭信息
- 所有级别的日志（INFO及以上）
- 业务操作日志
- 系统运行状态

**滚动策略**:
- 按天滚动
- 单个文件最大10MB
- 保留30天

### 2. blog-server-error.log
**用途**: 仅记录错误日志

**内容**:
- ERROR级别的日志
- 异常堆栈信息
- 系统错误详情

**滚动策略**:
- 按天滚动
- 单个文件最大10MB
- 保留30天

### 3. blog-server-api.log
**用途**: 记录API请求和响应

**内容**:
- 请求ID（用于追踪）
- HTTP方法和URI
- 请求参数（敏感信息已脱敏）
- 响应结果
- 执行时间
- 客户端IP
- User-Agent

**示例**:
```
2025-12-25 10:30:00.123 [http-nio-8080-exec-1] INFO  ApiLoggingAspect - API Request - ID: a1b2c3d4, Method: POST, URI: /api/articles, Query: null, IP: 192.168.1.100, UserAgent: Mozilla/5.0, Params: {"title":"Test Article","content":"..."}
2025-12-25 10:30:00.456 [http-nio-8080-exec-1] INFO  ApiLoggingAspect - API Response - ID: a1b2c3d4, Status: SUCCESS, Time: 333ms, Response: {"success":true,"data":1}
```

**滚动策略**:
- 按天滚动
- 单个文件最大10MB
- 保留30天

### 4. blog-server-security.log
**用途**: 记录安全相关事件

**内容**:
- 用户登录/注册尝试
- 认证失败
- 权限拒绝
- 敏感词检测
- SQL注入检测
- XSS攻击检测
- 管理员操作

**示例**:
```
2025-12-25 10:30:00.123 [http-nio-8080-exec-1] INFO  com.blog.security - User login attempt - Phone: 138****5678, IP: 192.168.1.100
2025-12-25 10:30:05.456 [http-nio-8080-exec-2] WARN  com.blog.security - SQL injection detection triggered - IP: 192.168.1.101
2025-12-25 10:30:10.789 [http-nio-8080-exec-3] INFO  com.blog.security - Admin operation - Method: reviewArticle, IP: 192.168.1.102
```

**滚动策略**:
- 按天滚动
- 单个文件最大10MB
- 保留90天（安全日志保留更长时间）

## 敏感信息脱敏

系统会自动对以下敏感信息进行脱敏处理：

### 1. 手机号
- **原始**: 13812345678
- **脱敏**: 138****5678
- **规则**: 保留前3位和后4位

### 2. 邮箱
- **原始**: test@example.com
- **脱敏**: te***@example.com
- **规则**: 保留前2位和@后的域名

### 3. 密码
- **原始**: mypassword123
- **脱敏**: ******
- **规则**: 完全隐藏

### 4. 身份证
- **原始**: 110101199001011234
- **脱敏**: 110101********1234
- **规则**: 保留前6位和后4位

### 5. 银行卡
- **原始**: 6222021234567891234
- **脱敏**: ************1234
- **规则**: 只保留后4位

### 6. 真实姓名
- **原始**: 张三
- **脱敏**: 张**
- **规则**: 保留姓氏

### 7. Token
- **原始**: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
- **脱敏**: eyJhbGciOi...
- **规则**: 只显示前10位

### 8. 验证码
- **原始**: 123456
- **脱敏**: ****
- **规则**: 完全隐藏

## 日志配置

### Logback配置文件

日志配置文件位于 `src/main/resources/logback-spring.xml`

主要配置项：

```xml
<!-- 日志文件路径 -->
<property name="LOG_PATH" value="logs"/>

<!-- 日志格式 -->
<property name="CONSOLE_LOG_PATTERN" 
          value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %highlight(%-5level) %cyan(%logger{36}) - %msg%n"/>

<!-- 滚动策略 -->
<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
    <fileNamePattern>${LOG_PATH}/${LOG_FILE}.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
    <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
        <maxFileSize>10MB</maxFileSize>
    </timeBasedFileNamingAndTriggeringPolicy>
    <maxHistory>30</maxHistory>
</rollingPolicy>
```

### Application配置

在 `application.yml` 中的日志配置：

```yaml
logging:
  level:
    root: INFO
    com.blog: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/blog-server.log
    max-size: 10MB
    max-history: 30
```

## 日志切面

### 1. ApiLoggingAspect

**功能**: 记录所有Controller层的API请求和响应

**切点**: `execution(* com.blog.controller..*(..))`

**记录内容**:
- 请求ID（UUID）
- HTTP方法和URI
- 查询参数
- 客户端IP
- User-Agent
- 请求参数（脱敏后）
- 响应结果（脱敏后）
- 执行时间

**实现位置**: `com.blog.aspect.ApiLoggingAspect`

### 2. SecurityLoggingAspect

**功能**: 记录安全相关的操作和事件

**切点**:
- `execution(* com.blog.service.impl.AuthServiceImpl.*(..))`
- `execution(* com.blog.service.impl.SecurityServiceImpl.*(..))`
- `execution(* com.blog.service.impl.AdminServiceImpl.*(..))`

**记录内容**:
- 用户登录/注册
- 敏感词检测
- SQL注入检测
- XSS攻击检测
- 管理员操作
- 安全异常

**实现位置**: `com.blog.aspect.SecurityLoggingAspect`

## 异步日志

系统使用异步日志提高性能：

```xml
<appender name="ASYNC_FILE_ALL" class="ch.qos.logback.classic.AsyncAppender">
    <appender-ref ref="FILE_ALL"/>
    <queueSize>512</queueSize>
    <discardingThreshold>0</discardingThreshold>
</appender>
```

**优点**:
- 不阻塞主线程
- 提高应用性能
- 队列大小512，足够应对高并发

## MDC（Mapped Diagnostic Context）

系统使用MDC在日志中添加上下文信息：

```java
// 添加请求ID到MDC
String requestId = UUID.randomUUID().toString().substring(0, 8);
MDC.put("requestId", requestId);

// 使用完后清理
MDC.remove("requestId");
```

**用途**:
- 追踪完整的请求链路
- 关联同一请求的所有日志
- 便于问题排查

## 环境配置

### 开发环境（dev）

```xml
<springProfile name="dev">
    <root level="DEBUG">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE_ALL"/>
        <appender-ref ref="ASYNC_FILE_ERROR"/>
    </root>
</springProfile>
```

**特点**:
- 日志级别：DEBUG
- 输出到控制台和文件
- 详细的调试信息

### 生产环境（prod）

```xml
<springProfile name="prod">
    <root level="INFO">
        <appender-ref ref="ASYNC_FILE_ALL"/>
        <appender-ref ref="ASYNC_FILE_ERROR"/>
    </root>
</springProfile>
```

**特点**:
- 日志级别：INFO
- 只输出到文件
- 减少日志量，提高性能

## 日志查看和分析

### 1. 实时查看日志

```bash
# 查看所有日志
tail -f logs/blog-server.log

# 查看错误日志
tail -f logs/blog-server-error.log

# 查看API日志
tail -f logs/blog-server-api.log

# 查看安全日志
tail -f logs/blog-server-security.log
```

### 2. 搜索日志

```bash
# 搜索特定请求ID的所有日志
grep "a1b2c3d4" logs/blog-server.log

# 搜索特定IP的所有请求
grep "192.168.1.100" logs/blog-server-api.log

# 搜索错误日志
grep "ERROR" logs/blog-server.log

# 搜索SQL注入尝试
grep "SQL injection" logs/blog-server-security.log
```

### 3. 日志分析

```bash
# 统计API请求数量
grep "API Request" logs/blog-server-api.log | wc -l

# 统计错误数量
grep "ERROR" logs/blog-server-error.log | wc -l

# 统计登录尝试次数
grep "User login attempt" logs/blog-server-security.log | wc -l

# 查看最慢的API请求
grep "API Response" logs/blog-server-api.log | grep -oP "Time: \d+ms" | sort -rn | head -10
```

## 日志维护

### 1. 日志清理

系统会自动清理过期日志：
- 普通日志保留30天
- 安全日志保留90天
- 超过保留期的日志会被自动删除

### 2. 日志压缩

系统会自动压缩历史日志：
- 使用gzip压缩
- 压缩后的文件名格式：`blog-server.2025-12-25.0.log.gz`
- 节省磁盘空间

### 3. 手动清理

如需手动清理日志：

```bash
# 删除30天前的日志
find logs/ -name "*.log.gz" -mtime +30 -delete

# 删除所有日志（谨慎操作）
rm -rf logs/*
```

## 最佳实践

### 1. 日志级别选择

- **ERROR**: 系统错误，需要立即处理
- **WARN**: 潜在问题，需要关注
- **INFO**: 重要业务操作，正常运行信息
- **DEBUG**: 调试信息，开发环境使用
- **TRACE**: 详细跟踪信息，问题排查使用

### 2. 日志内容

- 包含足够的上下文信息
- 避免记录敏感信息（或进行脱敏）
- 使用结构化的日志格式
- 包含请求ID便于追踪

### 3. 性能考虑

- 使用异步日志
- 避免在循环中记录大量日志
- 生产环境使用INFO级别
- 定期清理历史日志

### 4. 安全考虑

- 敏感信息必须脱敏
- 安全日志保留更长时间
- 限制日志文件访问权限
- 定期审计安全日志

## 故障排查

### 1. 日志文件未生成

**原因**: 日志目录不存在或无写入权限

**解决**:
```bash
mkdir -p logs
chmod 755 logs
```

### 2. 日志文件过大

**原因**: 日志级别设置过低或日志量过大

**解决**:
- 调整日志级别为INFO
- 减小maxFileSize
- 减少maxHistory

### 3. 日志丢失

**原因**: 异步日志队列满

**解决**:
- 增加queueSize
- 调整discardingThreshold

## 监控和告警

建议配置以下监控和告警：

1. **错误日志监控**: 当错误日志数量超过阈值时告警
2. **安全事件监控**: 当检测到SQL注入或XSS攻击时告警
3. **磁盘空间监控**: 当日志目录磁盘使用率超过80%时告警
4. **日志延迟监控**: 当异步日志队列积压时告警

## 总结

本系统实现了完整的日志记录功能，包括：

✅ 配置日志级别和格式  
✅ 记录API请求日志  
✅ 记录错误日志  
✅ 记录安全事件日志  
✅ 实现日志脱敏（敏感信息）  

所有日志都经过精心设计，既保证了系统的可观测性，又保护了用户的隐私安全。

**验证需求: 12.5** ✅
