# 权限控制实现文档

## 概述

本文档描述了个人博客系统的权限控制实现，包括方法级权限验证、访客权限限制、博主权限验证和管理员权限验证。

## 权限角色

系统定义了三种用户角色：

1. **访客 (Visitor)**: 未登录用户
2. **注册用户/博主 (USER/BLOGGER)**: 已登录用户，博主拥有额外权限
3. **管理员 (ADMIN)**: 平台管理员

## 权限配置

### Spring Security配置

在 `SecurityConfig.java` 中启用了方法级权限验证：

```java
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
```

### URL级别权限控制

在 `SecurityConfig.filterChain()` 中配置了URL级别的权限：

- **公开接口** (无需认证):
  - `/auth/**` - 认证相关接口
  - `GET /articles` - 文章列表（访客可访问）
  - `GET /articles/search` - 文章搜索
  - `GET /categories/**` - 分类查询
  - `GET /tags/**` - 标签查询
  - `GET /users/*/space` - 博主个人空间
  - `GET /carousel` - 轮播图配置

- **管理员接口** (需要ADMIN角色):
  - `/admin/**` - 所有管理员功能

- **博主接口** (需要BLOGGER或ADMIN角色):
  - `POST /articles` - 创建文章
  - `PUT /articles/**` - 更新文章
  - `DELETE /articles/**` - 删除文章
  - `/users/space/settings` - 个人空间设置

- **其他接口**: 需要认证（isAuthenticated）

## 方法级权限注解

### @PreAuthorize 注解说明

系统使用Spring Security的 `@PreAuthorize` 注解在Service层方法上进行细粒度的权限控制。

#### 常用权限表达式

1. **`@PreAuthorize("isAuthenticated()")`**
   - 要求用户已登录
   - 适用于需要登录才能访问的功能

2. **`@PreAuthorize("hasRole('ADMIN')")`**
   - 要求用户具有ADMIN角色
   - 适用于管理员专属功能

3. **`@PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")`**
   - 要求用户具有BLOGGER或ADMIN角色
   - 适用于博主和管理员都可以访问的功能

## 权限控制实现

### 1. 访客权限限制 (需求 38.1-38.8)

访客（未登录用户）的权限限制：

#### 允许的操作
- 浏览首页文章列表（仅标题和摘要）
- 查看博主个人空间
- 搜索文章
- 查看分类和标签

#### 禁止的操作（需要登录）
- 查看文章详情
- 发表评论
- 点赞文章
- 收藏文章
- 打赏博主
- 关注博主
- 发送私信

#### 实现方式
- URL级别：在SecurityConfig中配置公开接口
- 方法级别：在需要登录的Service方法上添加 `@PreAuthorize("isAuthenticated()")`

#### 示例代码

```java
// UserServiceImpl.java
@Override
@Transactional(readOnly = true)
@PreAuthorize("isAuthenticated()")
public UserProfileResponse getUserProfile(Long userId) {
    // 需要登录才能查看用户信息
}

// InteractionServiceImpl.java
@Override
@Transactional
@PreAuthorize("isAuthenticated()")
public boolean toggleLike(Long userId, Long articleId) {
    // 需要登录才能点赞
}

// CommentServiceImpl.java
@Override
@Transactional
@PreAuthorize("isAuthenticated()")
public Comment createComment(Comment comment) {
    // 需要登录才能评论
}

// MessageServiceImpl.java
@Override
@Transactional
@PreAuthorize("isAuthenticated()")
public Message sendMessage(Message message) {
    // 需要登录才能发送私信
}

// WalletServiceImpl.java
@Override
@Transactional
@PreAuthorize("isAuthenticated()")
public Wallet recharge(Long userId, BigDecimal amount) {
    // 需要登录才能充值
}

// BrowseHistoryServiceImpl.java
@Override
@Transactional
@PreAuthorize("isAuthenticated()")
public BrowseHistory recordBrowseHistory(Long userId, Long articleId) {
    // 需要登录才能记录浏览历史
}
```

### 2. 博主权限控制 (需求 39.1-39.6)

博主专属功能的权限控制：

#### 博主专属功能
- 发布文章
- 编辑文章
- 删除文章
- 置顶文章
- 设置公告
- 个人空间设置
- 查看钱包收益

#### 实现方式
- 在Service方法上添加 `@PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")`
- 在方法内部验证用户是否是文章作者（只能操作自己的文章）

#### 示例代码

```java
// ArticleServiceImpl.java
@Override
@Transactional
@PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
public Article createArticle(Article article) {
    // 验证用户是否是博主
    User user = userRepository.findById(article.getUserId())
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    
    if (user.getRole() != User.UserRole.BLOGGER && user.getRole() != User.UserRole.ADMIN) {
        throw new BusinessException("PERMISSION_DENIED", "只有博主才能发布文章");
    }
    // ...
}

@Override
@Transactional
@PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
public Article updateArticle(Long id, Article article) {
    // 验证权限：只有文章作者可以编辑
    Article existingArticle = articleRepository.findById(id)
        .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
    
    if (!existingArticle.getUserId().equals(article.getUserId())) {
        throw new BusinessException("PERMISSION_DENIED", "只能编辑自己的文章");
    }
    // ...
}

@Override
@Transactional
@PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
public void deleteArticle(Long id, Long userId) {
    // 验证权限：只有文章作者可以删除
    Article article = articleRepository.findById(id)
        .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
    
    if (!article.getUserId().equals(userId)) {
        throw new BusinessException("PERMISSION_DENIED", "只能删除自己的文章");
    }
    // ...
}

@Override
@Transactional
@PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
public Article pinArticle(Long id, Long userId, boolean isPinned) {
    // 验证权限：只有文章作者可以置顶
    Article article = articleRepository.findById(id)
        .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
    
    if (!article.getUserId().equals(userId)) {
        throw new BusinessException("PERMISSION_DENIED", "只能置顶自己的文章");
    }
    // ...
}

// BloggerServiceImpl.java
@Override
@Transactional
@PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
public Announcement saveAnnouncement(Long userId, String content) {
    // 只有博主可以设置公告
}

@Override
@Transactional
@PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
public void deleteAnnouncement(Long userId) {
    // 只有博主可以删除公告
}

@Override
@Transactional
@PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
public SpaceSetting saveSpaceSettings(Long userId, String themeColor, 
                                     String backgroundImage, 
                                     SpaceSetting.LayoutStyle layoutStyle) {
    // 只有博主可以设置个人空间
}
```

### 3. 管理员权限验证 (需求 33-37)

管理员专属功能的权限控制：

#### 管理员专属功能
- 文章审核
- 博主申请审核
- 用户管理（禁用/启用用户）
- 轮播图管理
- 平台钱包管理
- 敏感词管理

#### 实现方式
- 在Service方法上添加 `@PreAuthorize("hasRole('ADMIN')")`

#### 示例代码

```java
// AdminServiceImpl.java
@Override
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public Article reviewArticle(Long articleId, boolean approved, String reviewComment) {
    // 只有管理员可以审核文章
}

@Override
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")
public Page<Article> getPendingArticles(Pageable pageable) {
    // 只有管理员可以查看待审核文章
}

@Override
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public BloggerApplication reviewBloggerApplication(Long applicationId, 
                                                   boolean approved, 
                                                   String reviewComment) {
    // 只有管理员可以审核博主申请
}

@Override
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public User updateUserStatus(Long userId, User.UserStatus status) {
    // 只有管理员可以更新用户状态
}

@Override
@Transactional(readOnly = true)
@PreAuthorize("hasRole('ADMIN')")
public Page<User> getAllUsers(Pageable pageable) {
    // 只有管理员可以查看所有用户
}

@Override
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public CarouselConfig addToCarousel(Long articleId, Integer displayOrder) {
    // 只有管理员可以添加轮播图
}

@Override
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public void removeFromCarousel(Long articleId) {
    // 只有管理员可以移除轮播图
}

// SecurityServiceImpl.java
@Override
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public SensitiveWord addSensitiveWord(String word) {
    // 只有管理员可以添加敏感词
}

@Override
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public void deleteSensitiveWord(String word) {
    // 只有管理员可以删除敏感词
}

@Override
@Transactional
@PreAuthorize("hasRole('ADMIN')")
public void importSensitiveWords(List<String> words) {
    // 只有管理员可以批量导入敏感词
}
```

## 权限验证流程

### 1. 请求处理流程

```
客户端请求
    ↓
JWT认证过滤器 (JwtAuthenticationFilter)
    ↓
提取JWT令牌并验证
    ↓
设置SecurityContext
    ↓
URL级别权限检查 (SecurityConfig)
    ↓
Controller层
    ↓
方法级别权限检查 (@PreAuthorize)
    ↓
Service层业务逻辑
    ↓
返回响应
```

### 2. 权限验证失败处理

- **401 Unauthorized**: 未登录或令牌无效
  - 由 `JwtAuthenticationEntryPoint` 处理
  - 返回统一的错误响应

- **403 Forbidden**: 权限不足
  - 由 `GlobalExceptionHandler` 处理
  - 返回统一的错误响应

## 权限控制最佳实践

### 1. 双重验证

对于敏感操作，采用双重验证机制：
- 第一层：`@PreAuthorize` 注解验证角色
- 第二层：方法内部验证资源所有权

示例：
```java
@PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
public Article updateArticle(Long id, Article article) {
    // 第一层：已验证用户是博主或管理员
    
    // 第二层：验证用户是否是文章作者
    Article existingArticle = articleRepository.findById(id)
        .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
    
    if (!existingArticle.getUserId().equals(article.getUserId())) {
        throw new BusinessException("PERMISSION_DENIED", "只能编辑自己的文章");
    }
    // ...
}
```

### 2. 最小权限原则

- 默认拒绝所有访问
- 只授予必要的最小权限
- 公开接口明确配置为 `permitAll()`

### 3. 权限注解位置

- 优先在Service层添加权限注解
- Controller层可以添加额外的权限注解作为第一道防线
- 避免在Repository层添加权限注解

### 4. 错误处理

- 使用统一的异常处理机制
- 返回友好的错误提示
- 记录安全相关的日志

## 测试建议

### 1. 单元测试

为每个权限控制点编写单元测试：
- 测试未登录用户访问受保护资源
- 测试普通用户访问博主专属功能
- 测试博主访问管理员专属功能
- 测试用户访问其他用户的资源

### 2. 集成测试

测试完整的权限验证流程：
- 测试JWT令牌验证
- 测试角色权限验证
- 测试资源所有权验证

## 权限矩阵

| 功能 | 访客 | 注册用户 | 博主 | 管理员 |
|------|------|----------|------|--------|
| 浏览文章列表 | ✓ | ✓ | ✓ | ✓ |
| 查看文章详情 | ✗ | ✓ | ✓ | ✓ |
| 发表评论 | ✗ | ✓ | ✓ | ✓ |
| 点赞/收藏 | ✗ | ✓ | ✓ | ✓ |
| 关注博主 | ✗ | ✓ | ✓ | ✓ |
| 打赏博主 | ✗ | ✓ | ✓ | ✓ |
| 发送私信 | ✗ | ✓ | ✓ | ✓ |
| 发布文章 | ✗ | ✗ | ✓ | ✓ |
| 编辑文章 | ✗ | ✗ | ✓(自己的) | ✓ |
| 删除文章 | ✗ | ✗ | ✓(自己的) | ✓ |
| 置顶文章 | ✗ | ✗ | ✓(自己的) | ✓ |
| 设置公告 | ✗ | ✗ | ✓ | ✓ |
| 个人空间设置 | ✗ | ✗ | ✓ | ✓ |
| 文章审核 | ✗ | ✗ | ✗ | ✓ |
| 博主申请审核 | ✗ | ✗ | ✗ | ✓ |
| 用户管理 | ✗ | ✗ | ✗ | ✓ |
| 轮播图管理 | ✗ | ✗ | ✗ | ✓ |
| 敏感词管理 | ✗ | ✗ | ✗ | ✓ |

## 总结

本系统实现了完善的权限控制机制：

1. **多层次防护**: URL级别 + 方法级别 + 业务逻辑级别
2. **细粒度控制**: 使用 `@PreAuthorize` 注解实现方法级权限控制
3. **角色分离**: 清晰的角色定义和权限划分
4. **安全性**: 双重验证机制确保资源安全
5. **可维护性**: 统一的权限管理和错误处理

所有权限控制实现都符合需求文档中的规范（需求 38.1-38.8, 39.1-39.6），确保系统的安全性和可靠性。
