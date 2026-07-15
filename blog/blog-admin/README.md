# 博客管理后台

个人博客系统的管理员端应用，用于管理平台内容、用户和运营数据。

## 技术栈

- Vue 3 - 渐进式JavaScript框架
- Vite - 下一代前端构建工具
- Element Plus - Vue 3 UI组件库
- Vue Router - 官方路由管理器
- Pinia - Vue 3 状态管理库
- Axios - HTTP客户端

## 功能模块

- **仪表盘**: 平台数据概览
- **文章审核**: 审核博主提交的文章
- **用户管理**: 管理平台用户
- **轮播图管理**: 配置首页轮播图
- **博主申请**: 审核用户的博主申请
- **平台钱包**: 管理平台收益

## 开发指南

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:5174

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 项目结构

```
blog-admin/
├── src/
│   ├── layouts/          # 布局组件
│   │   └── AdminLayout.vue
│   ├── views/            # 页面组件
│   │   ├── Login.vue
│   │   ├── Dashboard.vue
│   │   ├── ArticleReview.vue
│   │   ├── UserManagement.vue
│   │   ├── CarouselManagement.vue
│   │   ├── BloggerApplications.vue
│   │   └── AdminWallet.vue
│   ├── stores/           # Pinia状态管理
│   │   └── admin.js
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── utils/            # 工具函数
│   │   └── request.js
│   ├── App.vue
│   └── main.js
├── .env.development      # 开发环境配置
├── .env.production       # 生产环境配置
└── vite.config.js        # Vite配置
```

## 环境变量

- `VITE_API_BASE_URL`: 后端API地址

## 登录说明

管理员登录使用手机号+验证码方式，只有角色为ADMIN的用户才能登录管理后台。

## 开发计划

当前版本为基础架构搭建，各功能模块的详细实现将在后续任务中完成：

- [ ] 任务 49.1: 实现Dashboard仪表盘
- [ ] 任务 49.2: 实现ArticleReview文章审核页面
- [ ] 任务 49.3: 实现UserManagement用户管理页面
- [ ] 任务 49.4: 实现CarouselManagement轮播图管理页面
- [ ] 任务 49.5: 实现BloggerApplications博主申请审核页面
- [ ] 任务 49.6: 实现AdminWallet平台钱包页面
