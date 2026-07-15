<template>
  <div class="admin-page">
    <!-- 侧边栏 -->
    <div class="admin-sidebar">
      <div class="sidebar-header">
        <h2>管理系统</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="admin-menu"
        @select="handleMenuSelect"
      >
        <el-menu-item index="dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据统计</span>
        </el-menu-item>
        <el-menu-item index="users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="articles">
          <el-icon><Document /></el-icon>
          <span>文章审核</span>
        </el-menu-item>
        <el-menu-item index="blogger">
          <el-icon><UserFilled /></el-icon>
          <span>博主申请</span>
        </el-menu-item>
        <el-menu-item index="carousel">
          <el-icon><Picture /></el-icon>
          <span>轮播图管理</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-footer">
        <el-button @click="backToHome" text>返回首页</el-button>
      </div>
    </div>

    <!-- 主内容区 -->
    <div class="admin-content">
      <div class="content-header">
        <h3>{{ menuTitle }}</h3>
        <div class="header-right">
          <span>管理员：{{ userStore.userInfo?.nickname }}</span>
        </div>
      </div>

      <!-- 数据统计 -->
      <div v-if="activeMenu === 'dashboard'" class="dashboard-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-icon user-icon"><el-icon><User /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalUsers }}</div>
                <div class="stat-label">总用户数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-icon article-icon"><el-icon><Document /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalArticles }}</div>
                <div class="stat-label">总文章数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-icon view-icon"><el-icon><View /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalViews }}</div>
                <div class="stat-label">总浏览量</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <div class="stat-icon blogger-icon"><el-icon><Star /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ stats.totalBloggers }}</div>
                <div class="stat-label">博主数量</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px;">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>近7天浏览趋势</span>
              </template>
              <LineChart :data="viewTrendData" />
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>文章分类统计</span>
              </template>
              <PieChart :data="categoryData" />
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 用户管理 -->
      <div v-if="activeMenu === 'users'" class="users-section">
        <div class="section-toolbar">
          <el-input v-model="userSearch" placeholder="搜索用户" style="width: 300px;" clearable>
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
        <el-table :data="filteredUsers" style="width: 100%; margin-top: 20px;">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="头像" width="80">
            <template #default="{ row }">
              <el-avatar :src="row.avatar" :size="40" />
            </template>
          </el-table-column>
          <el-table-column prop="nickname" label="昵称" />
          <el-table-column prop="phone" label="手机号" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.role === 'BLOGGER' ? 'warning' : 'info'">
                {{ row.role === 'BLOGGER' ? '博主' : '普通用户' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
                {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="注册时间" width="180" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="viewUser(row)">查看</el-button>
              <el-button 
                size="small" 
                :type="row.status === 'ACTIVE' ? 'danger' : 'success'"
                @click="toggleUserStatus(row)"
              >
                {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 文章审核 -->
      <div v-if="activeMenu === 'articles'" class="articles-section">
        <el-tabs v-model="articleTab">
          <el-tab-pane label="待审核" name="pending">
            <el-table :data="pendingArticles" style="width: 100%;">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="author" label="作者" width="120" />
              <el-table-column prop="category" label="分类" width="100" />
              <el-table-column prop="createTime" label="提交时间" width="180" />
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" type="success" @click="approveArticle(row)">通过</el-button>
                  <el-button size="small" type="danger" @click="rejectArticle(row)">拒绝</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          <el-tab-pane label="已通过" name="approved">
            <el-table :data="approvedArticles" style="width: 100%;">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="title" label="标题" />
              <el-table-column prop="author" label="作者" width="120" />
              <el-table-column prop="viewCount" label="浏览量" width="100" />
              <el-table-column prop="approveTime" label="审核时间" width="180" />
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="{ row }">
                  <el-button size="small" @click="viewArticle(row)">查看</el-button>
                  <el-button size="small" type="danger" @click="deleteArticle(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 博主申请审核 -->
      <div v-if="activeMenu === 'blogger'" class="blogger-section">
        <el-table :data="bloggerApplications" style="width: 100%;">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="申请人" width="200">
            <template #default="{ row }">
              <div style="display: flex; align-items: center; gap: 10px;">
                <el-avatar :src="row.avatar" :size="40" />
                <span>{{ row.nickname }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="申请理由" />
          <el-table-column prop="articleCount" label="文章数" width="100" />
          <el-table-column prop="applyTime" label="申请时间" width="180" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="success" @click="approveBlogger(row)">通过</el-button>
              <el-button size="small" type="danger" @click="rejectBlogger(row)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 轮播图管理 -->
      <div v-if="activeMenu === 'carousel'" class="carousel-section">
        <div class="section-toolbar">
          <el-button type="primary" @click="showAddCarousel = true">
            <el-icon><Plus /></el-icon> 添加轮播图
          </el-button>
        </div>
        <el-table :data="carouselList" style="width: 100%; margin-top: 20px;">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column label="图片" width="200">
            <template #default="{ row }">
              <el-image :src="row.image" style="width: 150px; height: 80px;" fit="cover" />
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="link" label="链接" />
          <el-table-column prop="sort" label="排序" width="80" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-switch v-model="row.status" @change="toggleCarouselStatus(row)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button size="small" @click="editCarousel(row)">编辑</el-button>
              <el-button size="small" type="danger" @click="deleteCarousel(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 添加轮播图对话框 -->
    <el-dialog v-model="showAddCarousel" title="添加轮播图" width="600px">
      <el-form :model="carouselForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="carouselForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            class="carousel-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeCarouselUpload"
          >
            <img v-if="carouselForm.image" :src="carouselForm.image" class="carousel-image" />
            <el-icon v-else class="carousel-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="链接">
          <el-input v-model="carouselForm.link" placeholder="请输入跳转链接" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="carouselForm.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddCarousel = false">取消</el-button>
        <el-button type="primary" @click="saveCarousel">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DataAnalysis, User, Document, UserFilled, Picture, View, Star,
  Search, Plus
} from '@element-plus/icons-vue'
import LineChart from '@/components/LineChart.vue'
import PieChart from '@/components/PieChart.vue'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = ref('dashboard')
const userSearch = ref('')
const articleTab = ref('pending')
const showAddCarousel = ref(false)

// 数据统计
const stats = ref({
  totalUsers: 1258,
  totalArticles: 3642,
  totalViews: 156789,
  totalBloggers: 89
})

// 浏览趋势数据
const viewTrendData = ref({
  dates: ['03-08', '03-09', '03-10', '03-11', '03-12', '03-13', '03-14'],
  views: [12580, 15230, 18960, 16420, 19850, 22340, 25680]
})

// 文章分类数据
const categoryData = ref([
  { value: 1235, name: '技术' },
  { value: 856, name: '生活' },
  { value: 642, name: '历史' },
  { value: 523, name: '文化' },
  { value: 386, name: '其他' }
])

// 菜单标题
const menuTitle = computed(() => {
  const titles = {
    dashboard: '数据统计',
    users: '用户管理',
    articles: '文章审核',
    blogger: '博主申请审核',
    carousel: '轮播图管理'
  }
  return titles[activeMenu.value]
})

// 用户列表
const users = ref([
  { id: 1, nickname: '胡雪岩', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', phone: '138****1234', role: 'BLOGGER', status: 'ACTIVE', createTime: '2025-01-15 10:30:00' },
  { id: 2, nickname: '用户2001', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=user2001', phone: '139****5678', role: 'USER', status: 'ACTIVE', createTime: '2025-02-20 14:20:00' },
  { id: 3, nickname: '用户3002', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=user3002', phone: '136****9012', role: 'USER', status: 'BANNED', createTime: '2025-03-10 09:15:00' }
])

const filteredUsers = computed(() => {
  if (!userSearch.value) return users.value
  return users.value.filter(u => 
    u.nickname.includes(userSearch.value) || u.phone.includes(userSearch.value)
  )
})

// 待审核文章
const pendingArticles = ref([
  { id: 101, title: '晚清商业变革与现代启示', author: '胡雪岩', category: '历史', createTime: '2025-03-14 10:30:00' },
  { id: 102, title: 'Vue3组合式API实践', author: '用户2001', category: '技术', createTime: '2025-03-14 11:20:00' }
])

// 已通过文章
const approvedArticles = ref([
  { id: 1, title: '胡雪岩的商业智慧', author: '胡雪岩', viewCount: 15680, approveTime: '2025-03-10 09:00:00' },
  { id: 2, title: '徽商精神解读', author: '胡雪岩', viewCount: 12450, approveTime: '2025-03-11 14:30:00' }
])

// 博主申请
const bloggerApplications = ref([
  { id: 1, nickname: '用户2001', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=user2001', reason: '热爱写作，希望分享技术经验', articleCount: 15, applyTime: '2025-03-14 09:00:00' },
  { id: 2, nickname: '用户3002', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=user3002', reason: '专注历史研究，想要传播文化', articleCount: 8, applyTime: '2025-03-13 16:30:00' }
])

// 轮播图列表
const carouselList = ref([
  { id: 1, image: 'https://picsum.photos/800/400?random=1', title: '欢迎来到个人博客', link: '/', sort: 1, status: true },
  { id: 2, image: 'https://picsum.photos/800/400?random=2', title: '热门文章推荐', link: '/article/1', sort: 2, status: true }
])

// 轮播图表单
const carouselForm = ref({
  title: '',
  image: '',
  link: '',
  sort: 0
})

// 菜单切换
const handleMenuSelect = (index) => {
  activeMenu.value = index
}

// 返回首页
const backToHome = () => {
  router.push('/')
}

// 用户管理
const viewUser = (user) => {
  ElMessage.info(`查看用户：${user.nickname}`)
}

const toggleUserStatus = (user) => {
  ElMessageBox.confirm(
    `确定要${user.status === 'ACTIVE' ? '禁用' : '启用'}用户 ${user.nickname} 吗？`,
    '提示',
    { type: 'warning' }
  ).then(() => {
    user.status = user.status === 'ACTIVE' ? 'BANNED' : 'ACTIVE'
    ElMessage.success('操作成功')
  }).catch(() => {})
}

// 文章审核
const approveArticle = (article) => {
  ElMessage.success(`文章《${article.title}》审核通过`)
  const index = pendingArticles.value.findIndex(a => a.id === article.id)
  if (index > -1) {
    pendingArticles.value.splice(index, 1)
  }
}

const rejectArticle = (article) => {
  ElMessageBox.prompt('请输入拒绝理由', '拒绝文章', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(({ value }) => {
    ElMessage.success(`文章《${article.title}》已拒绝`)
    const index = pendingArticles.value.findIndex(a => a.id === article.id)
    if (index > -1) {
      pendingArticles.value.splice(index, 1)
    }
  }).catch(() => {})
}

const viewArticle = (article) => {
  router.push(`/article/${article.id}`)
}

const deleteArticle = (article) => {
  ElMessageBox.confirm(`确定要删除文章《${article.title}》吗？`, '提示', {
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
  }).catch(() => {})
}

// 博主申请审核
const approveBlogger = (application) => {
  ElMessage.success(`已通过 ${application.nickname} 的博主申请`)
  const index = bloggerApplications.value.findIndex(a => a.id === application.id)
  if (index > -1) {
    bloggerApplications.value.splice(index, 1)
  }
}

const rejectBlogger = (application) => {
  ElMessageBox.prompt('请输入拒绝理由', '拒绝申请', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  }).then(({ value }) => {
    ElMessage.success(`已拒绝 ${application.nickname} 的博主申请`)
    const index = bloggerApplications.value.findIndex(a => a.id === application.id)
    if (index > -1) {
      bloggerApplications.value.splice(index, 1)
    }
  }).catch(() => {})
}

// 轮播图管理
const beforeCarouselUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    carouselForm.value.image = e.target.result
  }
  reader.readAsDataURL(file)
  return false
}

const saveCarousel = () => {
  if (!carouselForm.value.title || !carouselForm.value.image) {
    ElMessage.warning('请填写完整信息')
    return
  }
  carouselList.value.push({
    id: Date.now(),
    ...carouselForm.value,
    status: true
  })
  ElMessage.success('添加成功')
  showAddCarousel.value = false
  carouselForm.value = { title: '', image: '', link: '', sort: 0 }
}

const editCarousel = (carousel) => {
  carouselForm.value = { ...carousel }
  showAddCarousel.value = true
}

const deleteCarousel = (carousel) => {
  ElMessageBox.confirm('确定要删除这个轮播图吗？', '提示', {
    type: 'warning'
  }).then(() => {
    const index = carouselList.value.findIndex(c => c.id === carousel.id)
    if (index > -1) {
      carouselList.value.splice(index, 1)
    }
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const toggleCarouselStatus = (carousel) => {
  ElMessage.success(carousel.status ? '已启用' : '已禁用')
}
</script>

<style scoped>
.admin-page {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.admin-sidebar {
  width: 240px;
  background: #fff;
  box-shadow: 2px 0 8px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 30px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 20px;
  color: #2d3436;
  font-weight: 600;
}

.admin-menu {
  flex: 1;
  border-right: none;
}

.admin-menu .el-menu-item {
  height: 50px;
  line-height: 50px;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #f0f0f0;
}

.admin-content {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.content-header h3 {
  margin: 0;
  font-size: 24px;
  color: #2d3436;
  font-weight: 600;
}

.header-right {
  color: #636e72;
  font-size: 14px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;
  margin-right: 20px;
}

.user-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.article-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.view-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.blogger-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2d3436;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #636e72;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 16px;
  background: #f8fafc;
  border-radius: 8px;
}

.section-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.carousel-uploader {
  width: 100%;
}

.carousel-uploader :deep(.el-upload) {
  width: 100%;
  height: 200px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.carousel-uploader :deep(.el-upload:hover) {
  border-color: #f43f5e;
}

.carousel-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.carousel-uploader-icon {
  font-size: 48px;
  color: #8c939d;
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
