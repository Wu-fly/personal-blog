<template>
  <div class="user-center-page">
    <div class="user-center-container">
      <!-- 侧边菜单 -->
      <div class="sidebar">
        <el-menu :default-active="activeMenu" @select="handleMenuSelect">
          <el-menu-item index="profile">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </el-menu-item>
          <el-menu-item index="articles">
            <el-icon><Document /></el-icon>
            <span>我的文章</span>
          </el-menu-item>
          <el-menu-item index="collections">
            <el-icon><Collection /></el-icon>
            <span>我的收藏</span>
          </el-menu-item>
          <el-menu-item index="likes">
            <el-icon><Star /></el-icon>
            <span>我的点赞</span>
          </el-menu-item>
          <el-menu-item index="purchases">
            <el-icon><ShoppingCart /></el-icon>
            <span>已购文章</span>
          </el-menu-item>
          <el-menu-item index="history">
            <el-icon><Clock /></el-icon>
            <span>浏览历史</span>
          </el-menu-item>
          <el-menu-item index="security">
            <el-icon><Lock /></el-icon>
            <span>账号安全</span>
          </el-menu-item>
          <el-menu-item index="blogger" v-if="!isBlogger">
            <el-icon><Star /></el-icon>
            <span>申请博主</span>
          </el-menu-item>
        </el-menu>
      </div>

      <!-- 主内容区 -->
      <div class="main-content">
        <!-- 个人资料 -->
        <div v-if="activeMenu === 'profile'" class="content-section">
          <h3>个人资料</h3>
          <el-form :model="profileForm" label-width="100px" class="profile-form">
            <el-form-item label="头像">
              <el-upload class="avatar-uploader" action="#" :show-file-list="false" :before-upload="beforeAvatarUpload">
                <el-avatar :size="80" :src="profileForm.avatar" />
                <div class="upload-tip">点击更换</div>
              </el-upload>
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" maxlength="20" show-word-limit />
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input v-model="profileForm.bio" type="textarea" :rows="3" maxlength="200" show-word-limit />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="profileForm.gender">
                <el-radio value="male">男</el-radio>
                <el-radio value="female">女</el-radio>
                <el-radio value="secret">保密</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 我的文章 -->
        <div v-if="activeMenu === 'articles'" class="content-section">
          <div class="section-header">
            <h3>我的文章</h3>
            <el-button type="primary" @click="$router.push('/article-editor')">写文章</el-button>
          </div>
          <el-table :data="myArticles" style="width: 100%">
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="viewCount" label="阅读" width="80" />
            <el-table-column prop="createTime" label="发布时间" width="180" />
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button link type="primary" @click="editArticle(row.id)">编辑</el-button>
                <el-button link type="danger" @click="deleteArticle(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 我的收藏 -->
        <div v-if="activeMenu === 'collections'" class="content-section">
          <h3>我的收藏</h3>
          <div class="article-list">
            <ArticleCard v-for="article in collections" :key="article.id" :article="article" />
          </div>
        </div>

        <!-- 我的点赞 -->
        <div v-if="activeMenu === 'likes'" class="content-section">
          <h3>我的点赞</h3>
          <div class="article-list">
            <ArticleCard v-for="article in likes" :key="article.id" :article="article" />
          </div>
        </div>

        <!-- 已购文章 -->
        <div v-if="activeMenu === 'purchases'" class="content-section">
          <h3>已购文章</h3>
          <div class="article-list">
            <ArticleCard v-for="article in purchases" :key="article.id" :article="article" />
          </div>
        </div>

        <!-- 浏览历史 -->
        <div v-if="activeMenu === 'history'" class="content-section">
          <div class="section-header">
            <h3>浏览历史</h3>
            <el-button type="danger" link @click="clearHistory">清空历史</el-button>
          </div>
          <div class="history-list">
            <div v-for="item in browseHistory" :key="item.id" class="history-item" @click="$router.push(`/article/${item.articleId}`)">
              <img :src="item.coverImage" class="history-cover" />
              <div class="history-info">
                <h4>{{ item.title }}</h4>
                <p class="history-meta">
                  <span>{{ item.authorName }}</span>
                  <span>浏览于 {{ item.browseTime }}</span>
                </p>
              </div>
              <el-button class="delete-btn" link type="danger" @click.stop="deleteHistory(item.articleId)">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </div>
          <el-empty v-if="browseHistory.length === 0" description="暂无浏览历史" />
        </div>

        <!-- 账号安全 -->
        <div v-if="activeMenu === 'security'" class="content-section">
          <h3>账号安全</h3>
          <div class="security-item">
            <div class="security-info">
              <span class="label">手机号</span>
              <span class="value">{{ userInfo.phone }}</span>
            </div>
            <el-button type="primary" link>更换手机号</el-button>
          </div>
        </div>

        <!-- 申请博主 -->
        <div v-if="activeMenu === 'blogger'" class="content-section">
          <h3>申请成为博主</h3>
          <div v-if="bloggerApplication.status === 'none'" class="blogger-apply-form">
            <p class="apply-tip">成为博主后，您可以发布原创文章、设置付费内容、接受打赏等。</p>
            <el-form :model="bloggerForm" label-width="100px" class="profile-form">
              <el-form-item label="博主昵称" required>
                <el-input v-model="bloggerForm.nickname" placeholder="请输入您的博主昵称（2-50个字符）" maxlength="50" show-word-limit />
              </el-form-item>
              <el-form-item label="个人简介" required>
                <el-input v-model="bloggerForm.bio" type="textarea" :rows="4" placeholder="介绍一下自己，让读者更了解您（至少10个字符）" maxlength="500" show-word-limit />
              </el-form-item>
              <el-form-item label="擅长领域">
                <el-select v-model="bloggerForm.fields" multiple placeholder="选择您擅长的领域" @change="handleFieldChange">
                  <el-option label="历史文化" value="history" />
                  <el-option label="商业智慧" value="business" />
                  <el-option label="技术开发" value="tech" />
                  <el-option label="生活感悟" value="life" />
                  <el-option label="读书笔记" value="reading" />
                  <el-option label="其他" value="other" />
                </el-select>
              </el-form-item>
              <el-form-item label="自定义领域" v-if="bloggerForm.fields.includes('other')">
                <el-input 
                  v-model="bloggerForm.customField" 
                  placeholder="请输入您的自定义领域（如：摄影、音乐、旅行等）" 
                  maxlength="20" 
                  show-word-limit 
                />
                <div class="field-tip">输入后，该领域将作为文章分类添加到首页</div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="submitBloggerApplication">提交申请</el-button>
              </el-form-item>
            </el-form>
          </div>
          <div v-else-if="bloggerApplication.status === 'pending'" class="application-status">
            <el-result icon="info" title="申请审核中" sub-title="您的博主申请正在审核中，请耐心等待...">
              <template #extra>
                <p class="apply-time">申请时间：{{ bloggerApplication.applyTime }}</p>
                <el-button type="warning" @click="handleCancelApplication">取消申请</el-button>
              </template>
            </el-result>
          </div>
          <div v-else-if="bloggerApplication.status === 'rejected'" class="application-status">
            <el-result icon="error" title="申请未通过" :sub-title="bloggerApplication.rejectReason">
              <template #extra>
                <el-button type="primary" @click="bloggerApplication.status = 'none'">重新申请</el-button>
              </template>
            </el-result>
          </div>
        </div>
      </div>
    </div>
    <BackToTop />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Document, Collection, Star, ShoppingCart, Lock, Clock, Delete } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import ArticleCard from '@/components/ArticleCard.vue'
import BackToTop from '@/components/BackToTop.vue'

const router = useRouter()
const userStore = useUserStore()

const activeMenu = ref('profile')
const userInfo = ref({})

const profileForm = reactive({
  avatar: '',
  nickname: '',
  bio: '',
  gender: 'secret'
})

const myArticles = ref([])
const collections = ref([])
const likes = ref([])
const purchases = ref([])
const browseHistory = ref([])
const isBlogger = ref(false)
const bloggerApplication = ref({ status: 'none', applyTime: '', rejectReason: '' })
const bloggerForm = reactive({
  nickname: '',
  bio: '',
  fields: [],
  customField: ''
})

// 模拟数据
const mockMyArticles = [
  { id: 1, title: '2025年前端开发趋势：AI驱动的开发工具崛起', status: 'published', viewCount: 12580, createTime: '2025-12-20' },
  { id: 2, title: 'Vue 3.5 新特性详解', status: 'published', viewCount: 8920, createTime: '2025-12-18' },
  { id: 3, title: '我的第一篇博客草稿', status: 'draft', viewCount: 0, createTime: '2025-12-22' },
  { id: 4, title: '待审核的技术文章', status: 'pending', viewCount: 0, createTime: '2025-12-21' }
]

const mockCollections = [
  {
    id: 4,
    title: '程序员的健康生活指南',
    summary: '长时间坐在电脑前工作对身体健康有很大影响...',
    coverImage: 'https://picsum.photos/800/400?random=4',
    authorId: 4,
    authorName: '健康达人',
    authorAvatar: 'https://picsum.photos/100/100?random=13',
    viewCount: 15230,
    likeCount: 1256,
    commentCount: 234,
    tags: ['健康', '生活'],
    createTime: '2025-12-12'
  }
]

const mockLikes = [
  {
    id: 6,
    title: '2025年最值得学习的编程语言排行榜',
    summary: '技术在不断发展，编程语言的流行度也在变化...',
    coverImage: 'https://picsum.photos/800/400?random=6',
    authorId: 1,
    authorName: '技术先锋',
    authorAvatar: 'https://picsum.photos/100/100?random=10',
    viewCount: 23450,
    likeCount: 1890,
    commentCount: 345,
    tags: ['编程语言', '技术趋势'],
    createTime: '2025-12-08'
  }
]

const mockPurchases = [
  {
    id: 3,
    title: '深入理解Spring Boot 3.0微服务架构设计',
    summary: '本文深入探讨Spring Boot 3.0在微服务架构中的最佳实践...',
    coverImage: 'https://picsum.photos/800/400?random=3',
    authorId: 3,
    authorName: 'Java架构师',
    authorAvatar: 'https://picsum.photos/100/100?random=12',
    viewCount: 6750,
    likeCount: 445,
    commentCount: 67,
    isPaid: true,
    price: 9.9,
    tags: ['Java', 'Spring Boot'],
    createTime: '2025-12-15'
  }
]

const mockBrowseHistory = [
  { id: 1, articleId: 1, title: '胡雪岩的商业智慧：从学徒到红顶商人', coverImage: 'https://picsum.photos/200/120?random=20', authorName: '胡雪岩', browseTime: '2025-12-25 10:30' },
  { id: 2, articleId: 2, title: '2025年前端开发趋势：AI驱动的开发工具崛起', coverImage: 'https://picsum.photos/200/120?random=21', authorName: '技术先锋', browseTime: '2025-12-25 09:15' },
  { id: 3, articleId: 3, title: 'Vue 3.5 新特性详解', coverImage: 'https://picsum.photos/200/120?random=22', authorName: '前端达人', browseTime: '2025-12-24 20:45' },
  { id: 4, articleId: 4, title: '程序员的健康生活指南', coverImage: 'https://picsum.photos/200/120?random=23', authorName: '健康达人', browseTime: '2025-12-24 16:20' },
  { id: 5, articleId: 5, title: '深入理解Spring Boot 3.0微服务架构设计', coverImage: 'https://picsum.photos/200/120?random=24', authorName: 'Java架构师', browseTime: '2025-12-23 14:00' }
]

const handleMenuSelect = (index) => {
  activeMenu.value = index
}

const handleFieldChange = (value) => {
  // 如果取消选择"其他"，清空自定义领域
  if (!value.includes('other')) {
    bloggerForm.customField = ''
  }
}

const getStatusType = (status) => {
  const types = { draft: 'info', pending: 'warning', published: 'success', rejected: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { draft: '草稿', pending: '审核中', published: '已发布', rejected: '已拒绝' }
  return texts[status] || status
}

const beforeAvatarUpload = async (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }

  try {
    // 上传头像到服务器
    const { uploadAvatar, updateUserProfile } = await import('@/api/user')
    const uploadData = await uploadAvatar(file)
    
    console.log('上传响应:', uploadData)
    
    // 响应拦截器已经解包,uploadData就是UploadResponse对象
    if (uploadData && uploadData.url) {
      profileForm.avatar = uploadData.url
      console.log('头像URL已更新:', uploadData.url)
      
      // 自动保存到数据库
      const userData = await updateUserProfile({
        nickname: profileForm.nickname,
        avatar: uploadData.url,
        bio: profileForm.bio
      })
      
      if (userData) {
        // 更新 store 中的用户信息
        userStore.updateUserInfo({
          avatar: userData.avatar || uploadData.url
        })
        
        ElMessage.success('头像上传成功')
      }
    } else {
      console.error('上传响应中没有URL:', uploadData)
      ElMessage.error('头像上传失败：响应格式错误')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败：' + (error.message || '请重试'))
  }
  
  return false
}

const saveProfile = async () => {
  try {
    const { updateUserProfile } = await import('@/api/user')
    const userData = await updateUserProfile({
      nickname: profileForm.nickname,
      avatar: profileForm.avatar,
      bio: profileForm.bio
    })
    
    console.log('保存响应:', userData)
    
    // 响应拦截器已经解包，userData就是UserProfileResponse对象
    if (userData) {
      // 更新 store 中的用户信息
      userStore.updateUserInfo({
        nickname: userData.nickname || profileForm.nickname,
        avatar: userData.avatar || profileForm.avatar,
        bio: userData.bio || profileForm.bio
      })
      
      ElMessage.success('保存成功')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败：' + (error.message || '请重试'))
  }
}

const editArticle = (id) => {
  router.push(`/article-editor/${id}`)
}

const loadMyArticles = async () => {
  try {
    const { getMyArticles } = await import('@/api/article')
    const articlesRes = await getMyArticles({ page: 0, size: 100 })
    console.log('我的文章响应:', articlesRes)
    
    // 响应拦截器已经解包，articlesRes就是data.data（Page对象）
    if (articlesRes && articlesRes.content) {
      // 后端返回的是Spring Data Page对象，使用content字段
      const articles = articlesRes.content || []
      myArticles.value = articles.map(article => ({
        id: article.id,
        title: article.title,
        // 映射后端的reviewStatus到前端的status
        status: article.reviewStatus === 'APPROVED' ? 'published' : 
                article.reviewStatus === 'PENDING' ? 'pending' : 
                article.reviewStatus === 'REJECTED' ? 'rejected' : 'draft',
        viewCount: article.viewCount || 0,
        createTime: article.createdAt ? new Date(article.createdAt).toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit'
        }) : ''
      }))
      console.log('处理后的文章列表:', myArticles.value)
    }
  } catch (error) {
    console.error('加载我的文章失败:', error)
    // 如果加载失败，使用空数组
    myArticles.value = []
  }
}

const deleteArticle = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除这篇文章吗？', '提示', { type: 'warning' })
    
    // 调用删除API
    const { deleteArticle: deleteArticleApi } = await import('@/api/article')
    await deleteArticleApi(id)
    
    ElMessage.success('删除成功')
    
    // 重新加载文章列表
    await loadMyArticles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文章失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const deleteHistory = async (articleId) => {
  try {
    const { deleteBrowseHistory } = await import('@/api/user')
    await deleteBrowseHistory(articleId)
    
    // 从列表中移除
    browseHistory.value = browseHistory.value.filter(item => item.articleId !== articleId)
    ElMessage.success('已删除')
  } catch (error) {
    console.error('删除浏览历史失败:', error)
    ElMessage.error('删除失败，请重试')
  }
}

const clearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定清空所有浏览历史吗？', '提示', { type: 'warning' })
    
    const { clearBrowseHistory } = await import('@/api/user')
    await clearBrowseHistory()
    
    browseHistory.value = []
    ElMessage.success('已清空')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空浏览历史失败:', error)
      ElMessage.error('清空失败，请重试')
    }
  }
}

const submitBloggerApplication = async () => {
  if (!bloggerForm.nickname.trim()) {
    ElMessage.warning('请输入博主昵称')
    return
  }
  if (bloggerForm.nickname.trim().length < 2) {
    ElMessage.warning('博主昵称至少需要2个字符')
    return
  }
  if (!bloggerForm.bio.trim()) {
    ElMessage.warning('请输入个人简介')
    return
  }
  if (bloggerForm.bio.trim().length < 10) {
    ElMessage.warning('个人简介至少需要10个字符')
    return
  }
  if (bloggerForm.fields.includes('other') && !bloggerForm.customField.trim()) {
    ElMessage.warning('请输入自定义领域')
    return
  }
  
  try {
    // 如果有自定义领域，将其添加到分类中
    if (bloggerForm.fields.includes('other') && bloggerForm.customField.trim()) {
      // 保存自定义分类到 localStorage，供首页使用
      const customCategories = JSON.parse(localStorage.getItem('customCategories') || '[]')
      const newCategory = {
        label: bloggerForm.customField.trim(),
        value: bloggerForm.customField.trim().toLowerCase().replace(/\s+/g, '-'),
        custom: true,
        createdAt: new Date().toISOString()
      }
      
      // 检查是否已存在
      if (!customCategories.find(cat => cat.value === newCategory.value)) {
        customCategories.push(newCategory)
        localStorage.setItem('customCategories', JSON.stringify(customCategories))
      }
    }
    
    // 调用后端API提交申请
    const { applyBlogger } = await import('@/api/user')
    await applyBlogger({
      nickname: bloggerForm.nickname,
      bio: bloggerForm.bio,
      fields: bloggerForm.fields.join(','),
      customField: bloggerForm.customField || null
    })
    
    // 更新本地状态
    bloggerApplication.value = {
      status: 'pending',
      applyTime: new Date().toLocaleString(),
      rejectReason: ''
    }
    
    ElMessage.success('申请已提交，请等待管理员审核')
  } catch (error) {
    console.error('提交博主申请失败:', error)
    ElMessage.error('提交失败：' + (error.message || '请重试'))
  }
}

const handleCancelApplication = async () => {
  try {
    await ElMessageBox.confirm('确定要取消博主申请吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const { cancelBloggerApplication } = await import('@/api/user')
    await cancelBloggerApplication()
    
    // 重置状态
    bloggerApplication.value = { status: 'none', applyTime: '', rejectReason: '' }
    
    ElMessage.success('申请已取消')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消申请失败:', error)
      ElMessage.error('取消失败：' + (error.message || '请重试'))
    }
  }
}

onMounted(async () => {
  // 从后端加载用户信息
  try {
    const { getUserProfile, getBloggerApplicationStatus } = await import('@/api/user')
    const userData = await getUserProfile()
    
    // 响应拦截器已经解包，userData就是UserProfileResponse对象
    console.log('用户信息加载成功:', userData)
    
    if (userData) {
      userInfo.value = userData
      userStore.setUserInfo(userData)
      
      // 使用后端返回的真实数据
      profileForm.avatar = userData.avatar || 'https://i.pravatar.cc/150?img=1'
      profileForm.nickname = userData.nickname || '用户'
      profileForm.bio = userData.bio || '这个人很懒，什么都没写~'
      profileForm.gender = userData.gender || 'secret'
      
      // 检查用户角色
      isBlogger.value = userData.role === 'BLOGGER' || userData.role === 'ADMIN'
    }
    
    // 加载博主申请状态
    if (!isBlogger.value) {
      try {
        const applicationData = await getBloggerApplicationStatus()
        console.log('博主申请状态:', applicationData)
        if (applicationData && applicationData.status) {
          // 根据后端返回的状态设置本地状态
          const statusMap = {
            'PENDING': 'pending',
            'APPROVED': 'approved',
            'REJECTED': 'rejected'
          }
          bloggerApplication.value = {
            status: statusMap[applicationData.status] || 'none',
            applyTime: applicationData.createdAt ? new Date(applicationData.createdAt).toLocaleString() : '',
            rejectReason: applicationData.reviewComment || ''
          }
        } else {
          // 没有申请记录
          bloggerApplication.value = { status: 'none', applyTime: '', rejectReason: '' }
        }
      } catch (error) {
        console.log('没有博主申请记录或加载失败:', error)
        bloggerApplication.value = { status: 'none', applyTime: '', rejectReason: '' }
      }
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
    // 使用store中的缓存数据
    userInfo.value = userStore.userInfo || { phone: '138****8888', nickname: '用户8888' }
    profileForm.avatar = userInfo.value.avatar || 'https://i.pravatar.cc/150?img=1'
    profileForm.nickname = userInfo.value.nickname || '用户8888'
    profileForm.bio = userInfo.value.bio || '这个人很懒，什么都没写~'
    profileForm.gender = userInfo.value.gender || 'secret'
  }
  
  // 加载我的文章（从后端API获取）
  await loadMyArticles()
  
  // 从后端API加载收藏列表
  try {
    const { getFavoriteList } = await import('@/api/interaction')
    const favoritesRes = await getFavoriteList(0, 100)
    console.log('收藏列表响应:', favoritesRes)
    
    if (favoritesRes && favoritesRes.content) {
      // 转换为文章格式
      collections.value = favoritesRes.content.map(favorite => ({
        id: favorite.article?.id || favorite.articleId,
        title: favorite.article?.title || '未知标题',
        summary: favorite.article?.summary || favorite.article?.content?.substring(0, 100) + '...',
        coverImage: favorite.article?.coverImage || 'https://picsum.photos/800/400?random=' + favorite.articleId,
        authorId: favorite.article?.user?.id,
        authorName: favorite.article?.user?.nickname || '未知作者',
        authorAvatar: favorite.article?.user?.avatar,
        viewCount: favorite.article?.viewCount || 0,
        likeCount: favorite.article?.likeCount || 0,
        commentCount: favorite.article?.commentCount || 0,
        tags: favorite.article?.tags || [],
        createTime: favorite.createdAt?.split('T')[0] || new Date().toISOString().split('T')[0]
      }))
      console.log('处理后的收藏列表:', collections.value)
    }
  } catch (error) {
    console.error('加载收藏列表失败:', error)
    collections.value = []
  }
  
  // 从后端API加载点赞列表
  try {
    const { getLikeList } = await import('@/api/interaction')
    const likesRes = await getLikeList(0, 100)
    console.log('点赞列表响应:', likesRes)
    
    if (likesRes && likesRes.content) {
      // 转换为文章格式
      likes.value = likesRes.content.map(like => ({
        id: like.article?.id || like.articleId,
        title: like.article?.title || '未知标题',
        summary: like.article?.summary || like.article?.content?.substring(0, 100) + '...',
        coverImage: like.article?.coverImage || 'https://picsum.photos/800/400?random=' + like.articleId,
        authorId: like.article?.user?.id,
        authorName: like.article?.user?.nickname || '未知作者',
        authorAvatar: like.article?.user?.avatar,
        viewCount: like.article?.viewCount || 0,
        likeCount: like.article?.likeCount || 0,
        commentCount: like.article?.commentCount || 0,
        tags: like.article?.tags || [],
        createTime: like.createdAt?.split('T')[0] || new Date().toISOString().split('T')[0]
      }))
      console.log('处理后的点赞列表:', likes.value)
    }
  } catch (error) {
    console.error('加载点赞列表失败:', error)
    likes.value = []
  }
  
  // 从后端API加载已购文章列表
  try {
    const { getPurchaseList } = await import('@/api/interaction')
    const purchasesRes = await getPurchaseList(0, 100)
    console.log('已购文章列表响应:', purchasesRes)
    
    if (purchasesRes && purchasesRes.content) {
      // 转换为文章格式
      purchases.value = purchasesRes.content.map(purchase => ({
        id: purchase.article?.id || purchase.articleId,
        title: purchase.article?.title || '未知标题',
        summary: purchase.article?.summary || purchase.article?.content?.substring(0, 100) + '...',
        coverImage: purchase.article?.coverImage || 'https://picsum.photos/800/400?random=' + purchase.articleId,
        authorId: purchase.article?.user?.id,
        authorName: purchase.article?.user?.nickname || '未知作者',
        authorAvatar: purchase.article?.user?.avatar,
        viewCount: purchase.article?.viewCount || 0,
        likeCount: purchase.article?.likeCount || 0,
        commentCount: purchase.article?.commentCount || 0,
        isPaid: true,
        price: purchase.amount,
        tags: purchase.article?.tags || [],
        createTime: purchase.createdAt?.split('T')[0] || new Date().toISOString().split('T')[0]
      }))
      console.log('处理后的已购文章列表:', purchases.value)
    }
  } catch (error) {
    console.error('加载已购文章列表失败:', error)
    purchases.value = []
  }
  
  // 从后端API加载浏览历史
  try {
    const { getBrowseHistory } = await import('@/api/user')
    const historyRes = await getBrowseHistory({ page: 0, size: 100 })
    console.log('浏览历史响应:', historyRes)
    
    if (historyRes && historyRes.content) {
      // 转换为显示格式
      browseHistory.value = historyRes.content.map(history => ({
        id: history.id,
        articleId: history.article?.id || history.articleId,
        title: history.article?.title || '未知标题',
        coverImage: history.article?.coverImage || 'https://picsum.photos/200/120?random=' + history.articleId,
        authorName: history.article?.user?.nickname || '未知作者',
        browseTime: history.updatedAt ? new Date(history.updatedAt).toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        }) : '未知时间'
      }))
      console.log('处理后的浏览历史:', browseHistory.value)
    }
  } catch (error) {
    console.error('加载浏览历史失败:', error)
    browseHistory.value = []
  }
})
</script>


<style scoped>
.user-center-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.user-center-container {
  display: flex;
  gap: 30px;
}

.sidebar {
  width: 220px;
  background: #fff;
  border-radius: 20px;
  padding: 16px 0;
  height: fit-content;
  box-shadow: 0 4px 20px rgba(255, 107, 74, 0.08);
  border: 1px solid #fff5f0;
}

.sidebar :deep(.el-menu) {
  border-right: none;
}

.sidebar :deep(.el-menu-item) {
  margin: 4px 12px;
  border-radius: 10px;
}

.sidebar :deep(.el-menu-item:hover) {
  background: #fff8f5;
}

.main-content {
  flex: 1;
  background: #fff;
  border-radius: 20px;
  padding: 36px;
  box-shadow: 0 4px 20px rgba(255, 107, 74, 0.08);
  border: 1px solid #fff5f0;
}

.content-section h3 {
  margin-bottom: 24px;
  font-size: 20px;
  color: #2d3436;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.profile-form {
  max-width: 500px;
}

.avatar-uploader {
  position: relative;
  cursor: pointer;
}

.upload-tip {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(255, 107, 74, 0.8);
  color: #fff;
  font-size: 12px;
  text-align: center;
  padding: 4px;
  border-radius: 0 0 50% 50%;
}

.article-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #fff5f0;
}

.security-info .label {
  color: #636e72;
  margin-right: 16px;
}

.security-info .value {
  color: #2d3436;
  font-weight: 500;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.history-item:hover {
  background: #fff8f5;
  border-color: #ffe4d9;
}

.history-cover {
  width: 120px;
  height: 72px;
  object-fit: cover;
  border-radius: 8px;
}

.history-info {
  flex: 1;
}

.history-info h4 {
  margin: 0 0 8px;
  font-size: 15px;
  color: #2d3436;
  font-weight: 500;
}

.history-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #636e72;
  margin: 0;
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.3s;
}

.history-item:hover .delete-btn {
  opacity: 1;
}

.blogger-apply-form {
  max-width: 600px;
}

.apply-tip {
  color: #636e72;
  margin-bottom: 24px;
  padding: 16px;
  background: #fff8f5;
  border-radius: 12px;
  border-left: 4px solid #ff6b4a;
}

.application-status {
  text-align: center;
  padding: 40px 0;
}

.apply-time {
  color: #636e72;
  font-size: 14px;
}

.field-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #ff6b4a;
  line-height: 1.5;
}
</style>
