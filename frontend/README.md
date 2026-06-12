# FinSmart 智能记账前端

基于 Vue 3 + Element Plus 的现代化智能记账应用前端界面，参考 OPPO 记账 App 风格设计。

## 🎨 设计特色

- **整体风格**: 白色背景、圆角卡片、清爽简洁
- **主色调**: 紫色 (#6C5CE7)
- **布局**: 左侧导航栏 + 右侧内容区
- **响应式**: 完美适配桌面端和移动端

## 📱 页面功能

### 🏠 首页 (/dashboard)
- 本月支出统计卡片
- 今日收支概览
- 四大功能入口（2x2网格）
  - 本月账单 → 跳转到 /bill
  - AA共享 → 跳转到 /share  
  - 订阅提醒 → 跳转到 /subscription
  - 愿望清单 → 跳转到 /wish
- 最近账单列表

### 📝 账单页 (/bill)
- 月份筛选和收支类型筛选
- 分页账单列表
- 右下角悬浮新增按钮
- 新增/编辑账单弹窗
- 支持选择分类、金额、日期、备注

### 🔔 订阅管家 (/subscription)
- 本月订阅总支出统计
- 7天内到期订阅红色高亮
- 订阅卡片列表
- 支持新增、编辑、删除订阅
- 显示扣费周期和距离天数

### ⭐ 愿望清单 (/wish)
- 卡片式愿望展示
- 进度条显示（0-30%红色、30-70%橙色、70-100%绿色）
- 支持新增愿望、存入金额、删除
- 显示目标金额和预计达成日期

### 👥 AA共享 (/share)
- 共享账单列表
- 创建共享账单（支持多人分摊）
- 查看详情和成员付款状态
- 支持标记成员已付款
- 显示进度条和每人应付金额

### 🤖 AI分析 (/ai)
- 月份选择器
- AI智能洞察和建议
- 支出分类饼图
- 支出趋势折线图
- 环比变化和统计分析

## 🚀 技术栈

- **框架**: Vue 3 (Composition API)
- **UI组件**: Element Plus
- **路由**: Vue Router 4
- **状态管理**: Pinia
- **HTTP客户端**: Axios
- **图表库**: ECharts
- **日期处理**: Day.js
- **构建工具**: Vite
- **样式**: SCSS

## 📦 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口
│   │   ├── auth.js        # 认证接口
│   │   ├── bill.js        # 账单接口
│   │   ├── statistics.js  # 统计接口
│   │   ├── subscription.js # 订阅接口
│   │   ├── wish.js        # 愿望接口
│   │   ├── share.js       # 共享接口
│   │   ├── ai.js          # AI分析接口
│   │   └── category.js    # 分类接口
│   ├── components/        # 公共组件
│   ├── layout/           # 布局组件
│   │   └── index.vue     # 主布局
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── styles/           # 全局样式
│   │   └── global.scss
│   ├── utils/            # 工具函数
│   │   └── request.js    # HTTP请求封装
│   ├── views/            # 页面组件
│   │   ├── login/        # 登录页
│   │   ├── dashboard/    # 首页
│   │   ├── bill/         # 账单页
│   │   ├── subscription/ # 订阅页
│   │   ├── wish/         # 愿望清单
│   │   ├── share/        # AA共享
│   │   └── ai/           # AI分析
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML模板
├── package.json          # 依赖配置
├── vite.config.js        # Vite配置
└── README.md             # 项目说明
```

## 🛠️ 安装和运行

### 环境要求
- Node.js >= 16
- npm >= 8 或 yarn >= 1.22

### 安装依赖
```bash
cd frontend
npm install
# 或
yarn install
```

### 开发环境运行
```bash
npm run dev
# 或
yarn dev
```

应用将在 http://localhost:3000 启动

### 生产环境构建
```bash
npm run build
# 或
yarn build
```

构建文件将生成在 `dist/` 目录

### 预览生产构建
```bash
npm run preview
# 或
yarn preview
```

## 🔧 配置说明

### API代理配置
在 `vite.config.js` 中已配置代理，将 `/api` 请求代理到后端服务：
```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/api/, '')
  }
}
```

### 主题色配置
在 `src/styles/global.scss` 中可以修改主题色：
```scss
:root {
  --primary-color: #6C5CE7;
  --primary-light: #8B7FE6;
  --primary-dark: #5641D0;
  // ... 其他颜色变量
}
```

## 📋 API接口说明

所有API请求都会自动添加 Authorization 头：
```
Authorization: Bearer <token>
```

token 从 localStorage 中获取，在登录时保存。

### 主要接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/statistics/month` - 获取月度统计
- `GET /api/bill/page` - 分页获取账单
- `POST /api/bill` - 新增账单
- `PUT /api/bill` - 更新账单
- `DELETE /api/bill/{id}` - 删除账单
- `GET /api/subscription/list` - 获取订阅列表
- `GET /api/wish/list` - 获取愿望清单
- `GET /api/share/list` - 获取共享列表
- `GET /api/ai/analyze` - AI分析

## 🎯 特色功能

### 1. 响应式设计
- 完美适配桌面端和移动端
- 移动端优化的交互体验
- 自适应布局和字体大小

### 2. 现代化UI
- OPPO记账风格的圆角卡片设计
- 紫色主题配色方案
- 流畅的动画和过渡效果
- 清爽简洁的视觉体验

### 3. 完整功能
- 用户认证（登录/注册）
- 账单管理（增删改查）
- 订阅管理（到期提醒）
- 愿望清单（进度追踪）
- AA共享（多人分摊）
- AI智能分析

### 4. 数据可视化
- ECharts图表展示
- 支出分类饼图
- 支出趋势折线图
- 进度条可视化

## 🤝 开发说明

### 代码规范
- 使用 Vue 3 Composition API
- 统一的代码风格和命名规范
- 完善的错误处理和用户提示
- 响应式数据和计算属性

### 组件复用
- 抽象公共组件和工具函数
- 统一的样式和主题配置
- 可复用的业务逻辑

### 性能优化
- 路由懒加载
- 组件按需引入
- 图片和资源优化
- 构建产物压缩

## 📝 更新日志

### v1.0.0 (2024-04-26)
- ✨ 完成所有核心页面开发
- 🎨 实现OPPO记账风格UI设计
- 📱 完善响应式布局
- 🔧 配置完整的开发环境
- 📚 编写详细的项目文档

## 📄 许可证

MIT License

---

**FinSmart** - 智能记账，让生活更简单 💜
