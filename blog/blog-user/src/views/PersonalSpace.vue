<template>
  <div 
    class="personal-space-page" 
    :class="{ 'has-bg-image': currentBgImage }" 
    :style="[
      currentBgImage ? { '--bg-image': `url('${currentBgImage}')` } : {},
      { '--theme-color': spaceInfo.themeColor || '#f43f5e' }
    ]"
  >
    <!-- 动态背景装饰 -->
    <div v-if="!currentBgImage" class="bg-decorations">
      <div class="decoration decoration-1"></div>
      <div class="decoration decoration-2"></div>
      <div class="decoration decoration-3"></div>
      <div class="decoration decoration-4"></div>
      <div class="decoration decoration-5"></div>
      <div class="decoration decoration-6"></div>
      <div class="decoration decoration-7"></div>
      <div class="decoration decoration-8"></div>
      <div class="heart-decoration heart-1">❤</div>
      <div class="heart-decoration heart-2">❤</div>
      <div class="heart-decoration heart-3">❤</div>
      <div class="heart-decoration heart-4">❤</div>
      <div class="heart-decoration heart-5">❤</div>
      <div class="star-decoration star-1">⭐</div>
      <div class="star-decoration star-2">⭐</div>
      <div class="star-decoration star-3">⭐</div>
      <div class="star-decoration star-4">⭐</div>
    </div>
    
    <!-- 空间头部 -->
    <div class="space-header">
      <div class="header-overlay">
        <!-- 更换背景按钮 -->
        <el-button v-if="isOwner" circle size="small" class="change-bg-btn" @click="showBgUploadDialog = true">
          <el-icon><Picture /></el-icon>
        </el-button>
        
        <el-avatar :size="100" :src="spaceInfo.avatar" />
        <div class="name-edit-wrapper">
          <h2 class="space-name">{{ spaceInfo.nickname }}</h2>
          <el-button v-if="isOwner" circle size="small" class="edit-name-btn" @click="showEditDialog = true">
            <el-icon><Edit /></el-icon>
          </el-button>
        </div>
        <p class="space-bio">{{ spaceInfo.bio }}</p>
        <div class="space-stats">
          <span><strong>{{ spaceInfo.articleCount }}</strong> 文章</span>
          <span><strong>{{ spaceInfo.followerCount }}</strong> 粉丝</span>
          <span><strong>{{ spaceInfo.followingCount }}</strong> 关注</span>
        </div>
        <el-button v-if="!isOwner" :type="isFollowing ? 'default' : 'primary'" @click="toggleFollow">
          {{ isFollowing ? '已关注' : '关注' }}
        </el-button>
        <el-button v-if="!isOwner && isFollowing" type="primary" plain @click="sendPrivateMessage">
          <el-icon><ChatDotRound /></el-icon>
          发送私信
        </el-button>
        <el-button v-if="isOwner" type="primary" class="edit-space-btn" @click="$router.push('/space-settings')">
          <el-icon><Setting /></el-icon>
          编辑空间
        </el-button>
      </div>
    </div>

    <!-- 内容区 -->
    <div class="space-content">
      <!-- 公告栏 -->
      <el-card v-if="spaceInfo.announcement" class="announcement-card">
        <template #header>
          <div class="announcement-header">
            <div class="announcement-title">
              <el-icon><Bell /></el-icon>
              <span>空间公告</span>
            </div>
            <el-button v-if="isOwner" size="small" type="primary" link @click="$router.push('/space-settings')">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
          </div>
        </template>
        <div class="announcement-content">{{ spaceInfo.announcement }}</div>
      </el-card>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="文章" name="articles">
          <!-- 置顶文章 -->
          <div v-if="pinnedArticles.length > 0" class="pinned-section">
            <div class="section-title">
              <el-icon><Star /></el-icon>
              <span>置顶文章</span>
            </div>
            <div :class="['article-list', `layout-${spaceInfo.layout || 'card'}`]">
              <ArticleCard 
                v-for="article in pinnedArticles" 
                :key="article.id" 
                :article="article"
                :show-pin-badge="true"
                :show-edit-action="isOwner"
              />
            </div>
          </div>
          
          <!-- 普通文章 -->
          <div v-if="normalArticles.length > 0">
            <div v-if="pinnedArticles.length > 0" class="section-title">
              <span>全部文章</span>
            </div>
            <div :class="['article-list', `layout-${spaceInfo.layout || 'card'}`]">
              <ArticleCard 
                v-for="article in normalArticles" 
                :key="article.id" 
                :article="article"
                @pin="handlePinArticle(article)"
                :show-pin-action="isOwner"
                :show-edit-action="isOwner"
              />
            </div>
          </div>
          
          <div v-if="articles.length === 0" class="empty-state">
            <el-icon class="empty-icon"><Document /></el-icon>
            <p class="empty-text">还没有上传文章，快去试试吧</p>
            <el-button v-if="isOwner" type="primary" @click="$router.push('/editor')">创作文章</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane label="关注" name="following">
          <div v-if="followingList.length > 0" class="user-list">
            <div v-for="user in followingList" :key="user.id" class="user-item">
              <el-avatar :size="48" :src="user.avatar" @click="goToSpace(user.id)" style="cursor: pointer;" />
              <div class="user-info" @click="goToSpace(user.id)" style="cursor: pointer; flex: 1;">
                <span class="name">{{ user.nickname }}</span>
                <span class="bio">{{ user.bio }}</span>
              </div>
              <el-button 
                v-if="isOwner" 
                size="small" 
                type="danger" 
                plain 
                @click.stop="handleUnfollow(user.id)"
              >
                取消关注
              </el-button>
            </div>
          </div>
          <div v-else class="empty-state">
            <el-icon class="empty-icon"><User /></el-icon>
            <p class="empty-text">还没有关注的人呢</p>
          </div>
        </el-tab-pane>
        <el-tab-pane label="粉丝" name="followers">
          <div v-if="followerList.length > 0" class="user-list">
            <div v-for="user in followerList" :key="user.id" class="user-item" @click="goToSpace(user.id)">
              <el-avatar :size="48" :src="user.avatar" />
              <div class="user-info">
                <span class="name">{{ user.nickname }}</span>
                <span class="bio">{{ user.bio }}</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">
            <el-icon class="empty-icon"><UserFilled /></el-icon>
            <p class="empty-text">还没有粉丝，去发布文章试试吧</p>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
    
    <!-- 编辑昵称对话框 -->
    <el-dialog v-model="showEditDialog" title="修改昵称" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" maxlength="8" show-word-limit placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="editForm.bio" type="textarea" :rows="3" maxlength="50" show-word-limit placeholder="介绍一下自己吧" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="saveNickname">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 上传背景图片对话框 -->
    <el-dialog v-model="showBgUploadDialog" title="更换背景图片" width="500px">
      <el-upload
        class="bg-uploader"
        :action="uploadUrl"
        :headers="uploadHeaders"
        :show-file-list="false"
        :on-success="handleBgUploadSuccess"
        :before-upload="beforeBgUpload"
        :on-error="handleUploadError"
        drag
      >
        <div v-if="!showPreview" class="upload-placeholder">
          <el-icon class="upload-icon"><Plus /></el-icon>
          <div class="upload-text">点击或拖拽图片到此处上传</div>
          <div class="upload-tip">支持 JPG、PNG、WebP 格式，建议尺寸 1920x1080</div>
        </div>
        <div v-else class="bg-preview">
          <img :src="bgImagePreview" alt="背景预览" />
          <div class="preview-mask">
            <el-icon><Edit /></el-icon>
            <span>点击更换</span>
          </div>
        </div>
      </el-upload>
      <template #footer>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <el-button type="warning" plain @click="resetToDefaultBg">还原默认配置</el-button>
          <el-button @click="showBgUploadDialog = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
    
    <BackToTop />
  </div>
</template>


<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Edit, Document, User, UserFilled, Bell, Star, Setting, Picture, Plus, ChatDotRound } from '@element-plus/icons-vue'
import ArticleCard from '@/components/ArticleCard.vue'
import BackToTop from '@/components/BackToTop.vue'
import { getMyArticles, getUserArticles } from '@/api/article'
import { getUserSpace } from '@/api/user'
import { toggleFollow as toggleFollowAPI, getFollowStatus } from '@/api/interaction'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const spaceInfo = ref({
  id: '',
  nickname: '',
  avatar: '',
  bio: '',
  coverImage: '',
  bgImage: '',
  announcement: '',
  themeColor: '#f43f5e',
  layout: 'card',
  articleCount: 0,
  followerCount: 0,
  followingCount: 0
})

// 添加一个专门用于背景图片的响应式变量
const currentBgImage = ref('')

const activeTab = ref('articles')
const articles = ref([])
const followingList = ref([])
const followerList = ref([])
const isFollowing = ref(false)
const showEditDialog = ref(false)
const showBgUploadDialog = ref(false)
const bgImagePreview = ref('')
const editForm = ref({
  nickname: '',
  bio: ''
})

// 计算属性：是否显示预览
const showPreview = computed(() => {
  const hasPreview = bgImagePreview.value && bgImagePreview.value.trim() !== ''
  console.log('showPreview 计算:', hasPreview, 'bgImagePreview:', bgImagePreview.value)
  return hasPreview
})

// 上传配置
const uploadUrl = 'http://localhost:8080/api/upload/image'
const uploadHeaders = computed(() => {
  const token = userStore.token
  return token ? { 'Authorization': `Bearer ${token}` } : {}
})

const isOwner = computed(() => {
  return userStore.userInfo?.id && spaceInfo.value.id && 
         String(userStore.userInfo.id) === String(spaceInfo.value.id)
})

// 置顶文章
const pinnedArticles = computed(() => {
  return articles.value.filter(a => a.isPinned)
})

// 普通文章
const normalArticles = computed(() => {
  return articles.value.filter(a => !a.isPinned)
})

// 颜色转换函数
const hexToRgba = (hex, alpha) => {
  const r = parseInt(hex.slice(1, 3), 16)
  const g = parseInt(hex.slice(3, 5), 16)
  const b = parseInt(hex.slice(5, 7), 16)
  return `rgba(${r}, ${g}, ${b}, ${alpha})`
}

const toggleFollow = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  try {
    // 调用后端API切换关注状态
    const response = await toggleFollowAPI(spaceInfo.value.id)
    
    // 更新本地状态
    isFollowing.value = response.followed
    
    // 更新粉丝数
    if (response.followed) {
      spaceInfo.value.followerCount++
    } else {
      spaceInfo.value.followerCount--
    }
    
    // 更新 userStore 中的关注列表
    let followingIds = userStore.userInfo.followingIds || []
    if (response.followed) {
      // 添加到关注列表
      if (!followingIds.includes(spaceInfo.value.id)) {
        followingIds.push(spaceInfo.value.id)
      }
    } else {
      // 从关注列表移除
      followingIds = followingIds.filter(id => id !== spaceInfo.value.id)
    }
    
    // 保存到 userStore
    userStore.updateUserInfo({
      followingIds: followingIds
    })
    
    console.log('✓ 关注列表已更新:', followingIds)
    
    // 显示成功消息
    ElMessage.success(response.message || (response.followed ? '关注成功' : '取消关注成功'))
  } catch (error) {
    console.error('关注操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 取消关注（从关注列表中）
const handleUnfollow = async (userId) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  
  // 获取当前用户的关注列表
  let followingIds = userStore.userInfo.followingIds || []
  
  // 从关注列表中移除
  followingIds = followingIds.filter(id => id !== userId)
  
  // 保存到 userStore
  userStore.updateUserInfo({
    followingIds: followingIds
  })
  
  // 从显示列表中移除
  followingList.value = followingList.value.filter(user => user.id !== userId)
  
  // 找到被取消关注的用户名
  const unfollowedUser = allBloggers[userId]
  const userName = unfollowedUser ? unfollowedUser.nickname : '该用户'
  
  ElMessage.success(`已取消关注 ${userName}`)
}

const goToSpace = (userId) => {
  router.push(`/space/${userId}`)
}

// 发送私信
const sendPrivateMessage = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  
  // 跳转到私信页面，并传递接收者ID
  router.push({
    path: '/messages',
    query: { receiverId: spaceInfo.value.id }
  })
}

const handlePinArticle = (article) => {
  article.isPinned = !article.isPinned
  ElMessage.success(article.isPinned ? '文章已置顶' : '已取消置顶')
}

const saveNickname = () => {
  if (!editForm.value.nickname.trim()) {
    ElMessage.warning('请输入昵称')
    return
  }
  
  // 更新用户信息
  userStore.updateUserInfo({
    nickname: editForm.value.nickname,
    bio: editForm.value.bio
  })
  
  // 更新当前显示的空间信息
  spaceInfo.value.nickname = editForm.value.nickname
  spaceInfo.value.bio = editForm.value.bio
  
  showEditDialog.value = false
  ElMessage.success('修改成功')
}

// 上传前验证
const beforeBgUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG/WebP 格式的图片!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 上传成功
const handleBgUploadSuccess = async (response) => {
  console.log('=== 上传成功回调 ===')
  console.log('原始响应:', response)
  
  // el-upload 的 on-success 接收原始响应
  if (response && response.success && response.data && response.data.url) {
    // 直接使用返回的URL
    const imageUrl = response.data.url
    console.log('✓ 图片URL:', imageUrl)
    
    // 设置预览
    bgImagePreview.value = imageUrl
    console.log('✓ 预览已设置')
    
    // 立即保存到userStore
    userStore.updateUserInfo({ bgImage: imageUrl })
    console.log('✓ 已保存到 userStore')
    console.log('userStore.userInfo.bgImage:', userStore.userInfo.bgImage)
    console.log('sessionStorage:', JSON.parse(sessionStorage.getItem('userInfo')))
    
    // 更新 spaceInfo
    spaceInfo.value.bgImage = imageUrl
    console.log('✓ 已更新 spaceInfo.bgImage:', spaceInfo.value.bgImage)
    
    // 立即更新背景显示
    currentBgImage.value = imageUrl
    console.log('✓ 已更新 currentBgImage:', currentBgImage.value)
    
    // 等待 DOM 更新
    await nextTick()
    
    // 验证元素状态
    const contentEl = document.querySelector('.space-content')
    if (contentEl) {
      console.log('✓ 元素 classList:', contentEl.classList)
      console.log('✓ 元素 --bg-image:', contentEl.style.getPropertyValue('--bg-image'))
    }
    
    // 关闭对话框
    showBgUploadDialog.value = false
    
    ElMessage.success('背景图片更换成功！')
  } else {
    console.error('✗ 响应格式错误:', response)
    ElMessage.error('图片上传失败：响应格式错误')
  }
}

// 上传失败
const handleUploadError = () => {
  ElMessage.error('图片上传失败，请重试')
}

// 图片加载错误（移除此处理器，因为空字符串不应该触发加载）

// 保存背景图片（已废弃，现在上传后自动保存）
const saveBgImage = () => {
  ElMessage.info('背景已自动保存')
  showBgUploadDialog.value = false
}

// 还原默认背景配置
const resetToDefaultBg = () => {
  // 清空背景图片预览
  bgImagePreview.value = ''
  
  // 清空 userStore 中的背景图片
  userStore.updateUserInfo({ bgImage: '' })
  
  // 清空 spaceInfo 中的背景图片
  spaceInfo.value.bgImage = ''
  
  // 清空 currentBgImage，显示默认的蓝色天空背景
  currentBgImage.value = ''
  
  // 关闭对话框
  showBgUploadDialog.value = false
  
  ElMessage.success('已还原为默认背景')
}

// 监听路由参数变化
watch(() => route.params.id, (newId) => {
  if (newId) {
    loadSpaceInfo(newId)
  }
})

// 监听用户信息变化（当用户在设置页面保存后）
watch(() => userStore.userInfo, () => {
  if (isOwner.value) {
    loadSpaceInfo(route.params.id)
  }
}, { deep: true })

// 监听编辑对话框打开，同步当前信息
watch(showEditDialog, (newVal) => {
  if (newVal && isOwner.value) {
    // 如果是默认文字，则清空让用户输入
    editForm.value.nickname = spaceInfo.value.nickname === '未设置昵称' ? '' : spaceInfo.value.nickname
    editForm.value.bio = spaceInfo.value.bio === '这个人很懒，还没有填写个人简介' ? '' : spaceInfo.value.bio
  }
})

// 监听背景上传对话框打开，显示当前背景图片
watch(showBgUploadDialog, (newVal) => {
  if (newVal && isOwner.value) {
    // 只有当背景图片存在且不为空时才显示预览
    const currentBg = spaceInfo.value.bgImage
    bgImagePreview.value = (currentBg && currentBg.trim() !== '') ? currentBg : ''
    console.log('打开对话框，当前背景:', bgImagePreview.value)
  }
})

// 所有博主数据
const allBloggers = {
  '1': {
    id: '1',
    nickname: '胡雪岩',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan',
    bio: '红顶商人,徽商代表,一代商圣。经商之道,在于诚信为本,以义取利。'
  },
  '2': {
    id: '2',
    nickname: '左宗棠',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zuozongtang',
    bio: '晚清名臣,收复新疆,洋务运动的重要推动者'
  },
  '3': {
    id: '3',
    nickname: 'GGBond',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=ggbond',
    bio: '热爱技术,分享知识'
  },
  '4': {
    id: '4',
    nickname: '张謇',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangjian',
    bio: '状元实业家,中国近代民族工业的开拓者'
  },
  '5': {
    id: '5',
    nickname: '盛宣怀',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=shengxuanhuai',
    bio: '洋务运动代表人物,中国近代工业的奠基人'
  }
}

// 辅助函数:根据ID获取博主信息(处理类型转换)
const getBloggerById = (id) => {
  const stringId = String(id)
  return allBloggers[stringId] || { 
    id: stringId, 
    nickname: '用户' + stringId, 
    avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=user${stringId}`, 
    bio: '这个人很懒' 
  }
}

// 加载当前用户的文章列表
const loadMyArticles = async () => {
  try {
    const response = await getMyArticles({ page: 0, size: 100 })
    // 响应拦截器已经解包，response就是data.data（Page对象）
    if (response && response.content) {
      // 转换后端数据格式为前端需要的格式
      articles.value = response.content.map(article => ({
        id: article.id,
        title: article.title,
        summary: article.summary || article.content?.substring(0, 100) + '...',
        coverImage: article.coverImage || 'https://images.unsplash.com/photo-1499750310107-5fef28a66643',
        author: article.author?.nickname || '匿名',
        authorId: article.author?.id,
        authorAvatar: article.author?.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=default',
        viewCount: article.viewCount || 0,
        likeCount: article.likeCount || 0,
        commentCount: article.commentCount || 0,
        createTime: article.createdAt?.split('T')[0] || new Date().toISOString().split('T')[0],
        isPaid: article.isPaid || false,
        price: article.price || 0,
        isPinned: article.isPinned || false,
        tags: article.tags || [],
        status: article.reviewStatus || article.status
      }))
      
      // 更新文章数量
      spaceInfo.value.articleCount = articles.value.length
      
      console.log('加载文章成功:', articles.value.length, '篇')
    }
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('加载文章失败')
    articles.value = []
  }
}

// 加载空间信息
const loadSpaceInfo = async (userId) => {
  // 模拟数据 - 实际应该从API获取
  const currentUserId = String(userId || route.params.id)
  
  // 如果是胡雪岩（ID为3）- 优先判断，无论是否登录
  if (currentUserId === '3') {
    // 如果是登录的博主本人，使用 userStore 中的个性化设置
    const isOwnerLoggedIn = userStore.userInfo && String(userStore.userInfo.id) === '3'
    
    console.log('加载空间信息 - 用户ID 1')
    console.log('isOwnerLoggedIn:', isOwnerLoggedIn)
    console.log('userStore.userInfo.bgImage:', userStore.userInfo?.bgImage)
    
    spaceInfo.value = {
      id: '3',
      nickname: isOwnerLoggedIn ? (userStore.userInfo.nickname || '胡雪岩') : '胡雪岩',
      avatar: isOwnerLoggedIn ? (userStore.userInfo.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan') : 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan',
      bio: isOwnerLoggedIn ? (userStore.userInfo.bio || '红顶商人，徽商代表，一代商圣。经商之道，在于诚信为本，以义取利。') : '红顶商人，徽商代表，一代商圣。经商之道，在于诚信为本，以义取利。',
      coverImage: isOwnerLoggedIn ? (userStore.userInfo.coverImage || 'https://images.unsplash.com/photo-1579546929518-9e396f3cc809') : 'https://images.unsplash.com/photo-1579546929518-9e396f3cc809',
      bgImage: isOwnerLoggedIn ? (userStore.userInfo.bgImage || '') : '',
      announcement: isOwnerLoggedIn ? (userStore.userInfo.announcement || '欢迎来到胡雪岩的个人空间！在这里分享经商之道、人生智慧与历史故事。') : '欢迎来到胡雪岩的个人空间！在这里分享经商之道、人生智慧与历史故事。',
      themeColor: isOwnerLoggedIn ? (userStore.userInfo.themeColor || '#f43f5e') : '#f43f5e',
      layout: isOwnerLoggedIn ? (userStore.userInfo.layout || 'card') : 'card',
      articleCount: 10,
      followerCount: 12580,
      followingCount: 128
    }
    
    // 同步背景图片到 currentBgImage
    currentBgImage.value = spaceInfo.value.bgImage
    
    console.log('设置后的 spaceInfo.bgImage:', spaceInfo.value.bgImage)
    console.log('设置后的 currentBgImage:', currentBgImage.value)
    
    // 如果是本人登录，从API加载真实文章数据
    if (isOwnerLoggedIn) {
      await loadMyArticles()
    } else {
      // 否则使用模拟数据
      articles.value = [
        { id: 1, title: '胡雪岩的商业智慧：从学徒到红顶商人', summary: '探讨胡雪岩如何从一个钱庄学徒成长为清朝首富的传奇经历...', coverImage: 'https://images.unsplash.com/photo-1454165804606-c3d57bc86b40', author: '胡雪岩', authorId: '3', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', viewCount: 15680, likeCount: 2340, commentCount: 156, createTime: '2025-12-20', isPaid: true, price: 29.90, isPinned: true, tags: ['商业智慧', '历史人物', '创业故事'] },
        { id: 2, title: '徽商精神：诚信为本，以义取利', summary: '徽商文化的核心价值观及其对现代商业的启示...', coverImage: 'https://images.unsplash.com/photo-1450101499163-c8848c66ca85', author: '胡雪岩', authorId: '3', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', viewCount: 12450, likeCount: 1890, commentCount: 98, createTime: '2025-12-18', isPaid: false, isPinned: false, tags: ['徽商文化', '商业伦理', '传统文化'] },
        { id: 3, title: '从钱庄到银行：晚清金融变革', summary: '分析晚清时期金融体系的演变，以及胡雪岩在其中扮演的角色...', coverImage: 'https://images.unsplash.com/photo-1460925895917-afdab827c52f', author: '胡雪岩', authorId: '3', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', viewCount: 9870, likeCount: 1560, commentCount: 76, createTime: '2025-12-15', isPaid: true, price: 39.90, isPinned: false, tags: ['金融历史', '银行业', '晚清'] },
        { id: 4, title: '胡雪岩与左宗棠：商政联盟的典范', summary: '探讨胡雪岩与左宗棠之间的合作关系及其历史意义...', coverImage: 'https://images.unsplash.com/photo-1507679799987-c73779587ccf', author: '胡雪岩', authorId: '3', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', viewCount: 8650, likeCount: 1230, commentCount: 65, createTime: '2025-12-12', isPaid: false, isPinned: false, tags: ['历史人物', '政商关系', '晚清名臣'] },
        { id: 5, title: '晚清商业帝国的兴衰', summary: '回顾胡雪岩商业帝国的建立与崩塌，探讨其中的经验教训...', coverImage: 'https://images.unsplash.com/photo-1551836022-d5d88e9218df', author: '胡雪岩', authorId: '3', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', viewCount: 7890, likeCount: 1120, commentCount: 54, createTime: '2025-12-10', isPaid: false, isPinned: false, tags: ['商业案例', '历史教训', '企业管理'] },
        { id: 6, title: '红顶商人的政商智慧', summary: '分析胡雪岩如何在官场与商场之间游刃有余，成就一代传奇...', coverImage: 'https://images.unsplash.com/photo-1553877522-43269d4ea984', author: '胡雪岩', authorId: '3', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', viewCount: 6540, likeCount: 980, commentCount: 43, createTime: '2025-12-08', isPaid: true, price: 19.90, isPinned: false, tags: ['政商智慧', '人际关系', '成功之道'] },
        { id: 7, title: '胡庆余堂：百年药号的传承', summary: '探讨胡雪岩创办的胡庆余堂如何传承至今，成为中医药文化的瑰宝...', coverImage: 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d', author: '胡雪岩', authorId: '3', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', viewCount: 5670, likeCount: 850, commentCount: 38, createTime: '2025-12-05', isPaid: false, isPinned: false, tags: ['中医药', '品牌传承', '企业文化'] },
        { id: 8, title: '丝绸之路上的商业传奇', summary: '讲述胡雪岩如何通过丝绸贸易积累财富，开拓国际市场的故事...', coverImage: 'https://images.unsplash.com/photo-1559827260-dc66d52bef19', author: '胡雪岩', authorId: '3', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', viewCount: 4890, likeCount: 720, commentCount: 32, createTime: '2025-12-02', isPaid: false, isPinned: false, tags: ['丝绸贸易', '国际贸易', '商业拓展'] },
        { id: 9, title: '晚清金融危机与应对策略', summary: '分析胡雪岩在面对金融危机时的应对措施及其启示...', coverImage: 'https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3', author: '胡雪岩', authorId: '3', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', viewCount: 4120, likeCount: 650, commentCount: 28, createTime: '2025-11-28', isPaid: true, price: 24.90, isPinned: false, tags: ['金融危机', '风险管理', '应对策略'] },
        { id: 10, title: '慈善事业：商人的社会责任', summary: '探讨胡雪岩在慈善事业方面的贡献，以及商人的社会责任担当...', coverImage: 'https://images.unsplash.com/photo-1532629345422-7515f3d16bb6', author: '胡雪岩', authorId: '3', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', viewCount: 3560, likeCount: 580, commentCount: 24, createTime: '2025-11-25', isPaid: false, isPinned: false, tags: ['慈善事业', '社会责任', '企业家精神'] }
      ]
    }
    
    followingList.value = [
      { id: 2, nickname: '左宗棠', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zuozongtang', bio: '晚清名臣，收复新疆' },
      { id: 3, nickname: '盛宣怀', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=shengxuanhuai', bio: '洋务运动代表人物' },
      { id: 4, nickname: '张謇', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangjian', bio: '状元实业家' }
    ]
    followerList.value = [
      { id: 5, nickname: '红顶商人', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=hongding', bio: '商业爱好者' },
      { id: 6, nickname: '徽商后人', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huishang', bio: '传承徽商文化' },
      { id: 7, nickname: '商道学徒', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=shangdao', bio: '学习经商之道' },
      { id: 8, nickname: '历史探索者', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=lishi', bio: '热爱历史研究' }
    ]
    
    // 如果是当前登录用户，加载他的关注列表
    if (isOwner.value && userStore.userInfo.followingIds && userStore.userInfo.followingIds.length > 0) {
      followingList.value = userStore.userInfo.followingIds.map(id => {
        const blogger = getBloggerById(id)
        return blogger || { id, nickname: '用户' + id, avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=user${id}`, bio: '这个人很懒' }
      })
    }
  }
  // 其他知名博主
  else if (currentUserId === '2') {
    // 左宗棠
    spaceInfo.value = {
      id: '2',
      nickname: '左宗棠',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zuozongtang',
      bio: '晚清名臣，收复新疆，洋务运动的重要推动者',
      coverImage: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4',
      bgImage: '',
      announcement: '抬棺出征，誓死收复新疆！',
      themeColor: '#3b82f6',
      layout: 'card',
      articleCount: 5,
      followerCount: 8960,
      followingCount: 45
    }
    currentBgImage.value = spaceInfo.value.bgImage
    articles.value = [
      { id: 11, title: '收复新疆：一场艰苦卓绝的战争', summary: '回顾左宗棠西征收复新疆的历史过程...', coverImage: 'https://images.unsplash.com/photo-1451187580459-43490279c0fa', author: '左宗棠', authorId: '2', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zuozongtang', viewCount: 12340, likeCount: 1890, commentCount: 145, createTime: '2025-12-18', isPaid: false, isPinned: false, tags: ['历史', '军事', '新疆'] },
      { id: 12, title: '洋务运动中的福建船政局', summary: '探讨福建船政局在洋务运动中的重要作用...', coverImage: 'https://images.unsplash.com/photo-1436491865332-7a61a109cc05', author: '左宗棠', authorId: '2', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zuozongtang', viewCount: 9870, likeCount: 1450, commentCount: 98, createTime: '2025-12-15', isPaid: false, isPinned: false, tags: ['洋务运动', '近代化', '海军'] }
    ]
    followingList.value = [
      { id: 1, nickname: '胡雪岩', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', bio: '红顶商人，徽商代表' },
      { id: 3, nickname: '盛宣怀', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=shengxuanhuai', bio: '洋务运动代表人物' }
    ]
    followerList.value = [
      { id: 1, nickname: '胡雪岩', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', bio: '红顶商人，徽商代表' },
      { id: 5, nickname: '红顶商人', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=hongding', bio: '商业爱好者' }
    ]
    
    // 如果是当前登录用户，加载他的关注列表
    if (isOwner.value && userStore.userInfo.followingIds && userStore.userInfo.followingIds.length > 0) {
      followingList.value = userStore.userInfo.followingIds.map(id => {
        const blogger = getBloggerById(id)
        return blogger || { id, nickname: '用户' + id, avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=user${id}`, bio: '这个人很懒' }
      })
    }
  }
  else if (currentUserId === '3') {
    // 盛宣怀
    spaceInfo.value = {
      id: '3',
      nickname: '盛宣怀',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=shengxuanhuai',
      bio: '洋务运动代表人物，中国近代工业的奠基人',
      coverImage: 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b',
      bgImage: '',
      announcement: '实业救国，教育兴邦',
      themeColor: '#10b981',
      layout: 'double',
      articleCount: 6,
      followerCount: 7650,
      followingCount: 38
    }
    currentBgImage.value = spaceInfo.value.bgImage
    articles.value = [
      { id: 13, title: '轮船招商局的创办历程', summary: '讲述中国第一家轮船公司的创办故事...', coverImage: 'https://images.unsplash.com/photo-1464037866556-6812c9d1c72e', author: '盛宣怀', authorId: '3', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=shengxuanhuai', viewCount: 8760, likeCount: 1230, commentCount: 87, createTime: '2025-12-16', isPaid: false, isPinned: false, tags: ['洋务运动', '航运', '近代企业'] },
      { id: 14, title: '创办北洋大学堂的初衷', summary: '分享创办中国第一所现代大学的经历...', coverImage: 'https://images.unsplash.com/photo-1523050854058-8df90110c9f1', author: '盛宣怀', authorId: '3', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=shengxuanhuai', viewCount: 7890, likeCount: 1120, commentCount: 76, createTime: '2025-12-14', isPaid: false, isPinned: false, tags: ['教育', '大学', '近代化'] }
    ]
    followingList.value = [
      { id: 1, nickname: '胡雪岩', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', bio: '红顶商人，徽商代表' },
      { id: 2, nickname: '左宗棠', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zuozongtang', bio: '晚清名臣，收复新疆' }
    ]
    followerList.value = [
      { id: 1, nickname: '胡雪岩', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', bio: '红顶商人，徽商代表' },
      { id: 2, nickname: '左宗棠', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zuozongtang', bio: '晚清名臣，收复新疆' }
    ]
    
    // 如果是当前登录用户，加载他的关注列表
    if (isOwner.value && userStore.userInfo.followingIds && userStore.userInfo.followingIds.length > 0) {
      followingList.value = userStore.userInfo.followingIds.map(id => {
        const blogger = getBloggerById(id)
        return blogger || { id, nickname: '用户' + id, avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=user${id}`, bio: '这个人很懒' }
      })
    }
  }
  else if (currentUserId === '4') {
    // 张謇
    spaceInfo.value = {
      id: '4',
      nickname: '张謇',
      avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangjian',
      bio: '状元实业家，中国近代民族工业的开拓者',
      coverImage: 'https://images.unsplash.com/photo-1506905925346-21bda4d32df4',
      bgImage: '',
      announcement: '实业救国，教育立国',
      themeColor: '#f59e0b',
      layout: 'single',
      articleCount: 4,
      followerCount: 6540,
      followingCount: 32
    }
    currentBgImage.value = spaceInfo.value.bgImage
    articles.value = [
      { id: 15, title: '大生纱厂的创办与发展', summary: '讲述中国近代民族工业的典范企业...', coverImage: 'https://images.unsplash.com/photo-1581092160562-40aa08e78837', author: '张謇', authorId: '4', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangjian', viewCount: 6780, likeCount: 980, commentCount: 65, createTime: '2025-12-13', isPaid: false, isPinned: false, tags: ['实业', '纺织', '民族工业'] },
      { id: 16, title: '状元办实业的心路历程', summary: '从科举状元到实业家的转变...', coverImage: 'https://images.unsplash.com/photo-1497366216548-37526070297c', author: '张謇', authorId: '4', authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangjian', viewCount: 5890, likeCount: 870, commentCount: 54, createTime: '2025-12-11', isPaid: false, isPinned: false, tags: ['人物', '实业救国', '转型'] }
    ]
    followingList.value = [
      { id: 1, nickname: '胡雪岩', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', bio: '红顶商人，徽商代表' }
    ]
    followerList.value = [
      { id: 1, nickname: '胡雪岩', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', bio: '红顶商人，徽商代表' }
    ]
    
    // 如果是当前登录用户，加载他的关注列表
    if (isOwner.value && userStore.userInfo.followingIds && userStore.userInfo.followingIds.length > 0) {
      followingList.value = userStore.userInfo.followingIds.map(id => {
        const blogger = allBloggers[id]
        return blogger || { id, nickname: '用户' + id, avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=user${id}`, bio: '这个人很懒' }
      })
    }
  }
  // 其他普通用户
  else if (userStore.userInfo && String(userStore.userInfo.id) === currentUserId) {
    // 当前登录的普通用户
    spaceInfo.value = {
      id: userStore.userInfo.id,
      nickname: userStore.userInfo.nickname || '未设置昵称',
      avatar: userStore.userInfo.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=user${userStore.userInfo.id}`,
      bio: userStore.userInfo.bio || '这个人很懒，还没有填写个人简介',
      coverImage: userStore.userInfo.coverImage || 'https://images.unsplash.com/photo-1579546929518-9e396f3cc809',
      bgImage: userStore.userInfo.bgImage || '',
      announcement: userStore.userInfo.announcement || '',
      themeColor: userStore.userInfo.themeColor || '#f43f5e',
      layout: userStore.userInfo.layout || 'card',
      articleCount: 0,
      followerCount: 0,
      followingCount: 0
    }
    
    // 同步背景图片
    currentBgImage.value = spaceInfo.value.bgImage
    
    // 从API加载真实文章数据
    await loadMyArticles()
    
    // 加载当前用户的关注列表
    if (userStore.userInfo.followingIds && userStore.userInfo.followingIds.length > 0) {
      followingList.value = userStore.userInfo.followingIds.map(id => {
        const blogger = allBloggers[id]
        return blogger || { id, nickname: '用户' + id, avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=user${id}`, bio: '这个人很懒' }
      })
    } else {
      followingList.value = []
    }
    
    followerList.value = []
  }
  // 访问其他用户空间 - 从后端API获取真实数据
  else {
    try {
      console.log('从后端API获取用户空间信息，用户ID:', currentUserId)
      const response = await getUserSpace(currentUserId)
      console.log('后端返回的用户空间数据:', response)
      
      // 响应拦截器已经解包，response就是PersonalSpaceResponse对象
      if (response) {
        spaceInfo.value = {
          id: response.userId || currentUserId,
          nickname: response.nickname || '用户' + currentUserId,
          avatar: response.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=user${currentUserId}`,
          bio: response.bio || '这个人很懒，还没有填写个人简介',
          coverImage: 'https://images.unsplash.com/photo-1579546929518-9e396f3cc809',
          bgImage: response.spaceSettings?.backgroundImage || '',
          announcement: response.announcement || '',
          themeColor: response.spaceSettings?.themeColor || '#f43f5e',
          layout: response.spaceSettings?.layoutStyle?.toLowerCase() || 'card',
          articleCount: response.articleCount || 0,
          followerCount: response.followerCount || 0,
          followingCount: 0
        }
        
        currentBgImage.value = spaceInfo.value.bgImage
        console.log('✓ 用户空间信息加载成功:', spaceInfo.value.nickname)
      }
    } catch (error) {
      console.error('获取用户空间信息失败:', error)
      // 如果API调用失败，使用默认数据
      spaceInfo.value = {
        id: currentUserId,
        nickname: '用户' + currentUserId,
        avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=user${currentUserId}`,
        bio: '这个人很懒，还没有填写个人简介',
        coverImage: 'https://images.unsplash.com/photo-1579546929518-9e396f3cc809',
        bgImage: '',
        announcement: '',
        themeColor: '#f43f5e',
        layout: 'card',
        articleCount: 0,
        followerCount: 0,
        followingCount: 0
      }
      currentBgImage.value = ''
    }
    
    // 加载该用户的文章列表
    try {
      console.log('加载用户文章列表，用户ID:', currentUserId)
      const articlesResponse = await getUserArticles(currentUserId, { page: 0, size: 100 })
      console.log('后端返回的文章数据:', articlesResponse)
      
      if (articlesResponse && articlesResponse.content) {
        articles.value = articlesResponse.content.map(article => ({
          id: article.id,
          title: article.title,
          summary: article.summary || article.content?.substring(0, 100) + '...',
          coverImage: article.coverImage || 'https://images.unsplash.com/photo-1499750310107-5fef28a66643',
          author: article.author?.nickname || spaceInfo.value.nickname,
          authorId: article.author?.id || currentUserId,
          authorAvatar: article.author?.avatar || spaceInfo.value.avatar,
          viewCount: article.viewCount || 0,
          likeCount: article.likeCount || 0,
          commentCount: article.commentCount || 0,
          createTime: article.createdAt?.split('T')[0] || new Date().toISOString().split('T')[0],
          isPaid: article.isPaid || false,
          price: article.price || 0,
          isPinned: article.isPinned || false,
          tags: article.tags || [],
          status: article.reviewStatus || article.status
        }))
        console.log('✓ 文章列表加载成功:', articles.value.length, '篇')
      } else {
        articles.value = []
      }
    } catch (error) {
      console.error('加载用户文章列表失败:', error)
      articles.value = []
    }
    
    followingList.value = []
    followerList.value = []
  }
  
  // 加载关注状态（在空间信息加载完成后）
  if (userStore.isLoggedIn && !isOwner.value) {
    try {
      const response = await getFollowStatus(currentUserId)
      isFollowing.value = response.following || false
      console.log('关注状态已加载:', isFollowing.value)
    } catch (error) {
      console.error('加载关注状态失败:', error)
      isFollowing.value = false
    }
  } else {
    isFollowing.value = false
  }
}

onMounted(() => {
  // 清理所有旧的背景图片 URL
  const userInfo = userStore.userInfo
  if (userInfo && userInfo.bgImage) {
    console.log('当前背景图片 URL:', userInfo.bgImage)
    // 如果URL不包含/api/，清除它
    if (!userInfo.bgImage.includes('/api/')) {
      console.log('检测到旧的背景图片 URL，清除')
      userStore.updateUserInfo({ bgImage: '' })
    }
  }
  
  loadSpaceInfo()
  
  // 调试：检查背景样式
  setTimeout(() => {
    console.log('=== 页面加载完成后检查 ===')
    console.log('currentBgImage.value:', currentBgImage.value)
    console.log('spaceInfo.value.bgImage:', spaceInfo.value.bgImage)
    
    const contentEl = document.querySelector('.space-content')
    if (contentEl) {
      console.log('=== 内容区域背景样式 ===')
      console.log('classList:', contentEl.classList)
      console.log('style.--bg-image:', contentEl.style.getPropertyValue('--bg-image'))
      console.log('backgroundImage:', contentEl.style.backgroundImage)
      console.log('计算后的样式:', window.getComputedStyle(contentEl).backgroundImage)
    }
  }, 1000)
})
</script>


<style scoped>
.personal-space-page {
  min-height: 100vh;
  background: linear-gradient(180deg, 
    #87CEEB 0%,     /* 天蓝色 */
    #B0E0E6 30%,    /* 粉蓝色 */
    #E0F6FF 60%,    /* 浅蓝色 */
    #F0F8FF 100%    /* 爱丽丝蓝 */
  );
  position: relative;
  overflow: hidden;
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  25% {
    background-position: 50% 100%;
  }
  50% {
    background-position: 100% 50%;
  }
  75% {
    background-position: 50% 0%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 背景装饰容器 */
.bg-decorations {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
  overflow: hidden;
}

/* 云朵装饰 - 从左向右飘动 */
.decoration {
  position: absolute;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 100px;
  filter: blur(3px);
  animation: cloudDrift linear infinite;
}

/* 为云朵添加更真实的形状 */
.decoration::before,
.decoration::after {
  content: '';
  position: absolute;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
}

.decoration::before {
  width: 50%;
  height: 70%;
  top: -30%;
  left: 15%;
}

.decoration::after {
  width: 60%;
  height: 80%;
  top: -35%;
  right: 15%;
}

.decoration-1 {
  width: 150px;
  height: 60px;
  top: 10%;
  left: -200px;
  animation-duration: 45s;
  animation-delay: 0s;
}

.decoration-2 {
  width: 180px;
  height: 70px;
  top: 25%;
  left: -200px;
  animation-duration: 55s;
  animation-delay: 5s;
}

.decoration-3 {
  width: 120px;
  height: 50px;
  top: 40%;
  left: -200px;
  animation-duration: 40s;
  animation-delay: 10s;
}

.decoration-4 {
  width: 160px;
  height: 65px;
  top: 55%;
  left: -200px;
  animation-duration: 50s;
  animation-delay: 15s;
}

.decoration-5 {
  width: 140px;
  height: 55px;
  top: 70%;
  left: -200px;
  animation-duration: 48s;
  animation-delay: 20s;
}

.decoration-6 {
  width: 130px;
  height: 52px;
  top: 15%;
  left: -200px;
  animation-duration: 42s;
  animation-delay: 25s;
}

.decoration-7 {
  width: 110px;
  height: 45px;
  top: 50%;
  left: -200px;
  animation-duration: 38s;
  animation-delay: 30s;
}

.decoration-8 {
  width: 125px;
  height: 48px;
  top: 80%;
  left: -200px;
  animation-duration: 44s;
  animation-delay: 35s;
}

/* 心形装饰 - 移除，不再需要 */
.heart-decoration {
  display: none;
}

/* 星星装饰 - 移除，不再需要 */
.star-decoration {
  display: none;
}

/* 云朵从左向右飘动的动画 */
@keyframes cloudDrift {
  from {
    transform: translateX(0);
  }
  to {
    transform: translateX(calc(100vw + 250px));
  }
}

.personal-space-page.has-bg-image {
  background-image: var(--bg-image);
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  background-repeat: no-repeat;
  animation: none;
}

.personal-space-page.has-bg-image .bg-decorations {
  display: none;
}

.space-header {
  height: 380px;
  position: relative;
  background: transparent;
  z-index: 2;
}

.space-content {
  max-width: 1000px;
  margin: 20px auto 0;
  padding: 0 20px 40px;
  position: relative;
  z-index: 10;
  min-height: calc(100vh - 320px);
}

.header-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #2c3e50;
}

.change-bg-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.3);
  border: 1px solid rgba(44, 62, 80, 0.3);
  color: #2c3e50;
  transition: all 0.3s;
  backdrop-filter: blur(10px);
}

.change-bg-btn:hover {
  background: rgba(255, 255, 255, 0.5);
  border-color: rgba(44, 62, 80, 0.5);
  transform: scale(1.1);
}

.header-overlay .el-avatar {
  border: 4px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 30px rgba(44, 62, 80, 0.2);
}

.name-edit-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 20px 0 10px;
}

.space-name {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  color: #2c3e50;
  text-shadow: 0 2px 4px rgba(255, 255, 255, 0.8), 0 0 20px rgba(255, 255, 255, 0.5);
}

.edit-name-btn {
  background: rgba(255, 255, 255, 0.3);
  border: 1px solid rgba(44, 62, 80, 0.3);
  color: #2c3e50;
  transition: all 0.3s;
}

.edit-name-btn:hover {
  background: rgba(255, 255, 255, 0.5);
  border-color: rgba(44, 62, 80, 0.5);
  transform: scale(1.1);
}

.space-bio {
  opacity: 1;
  margin-bottom: 20px;
  font-size: 15px;
  color: #34495e;
  text-shadow: 0 1px 3px rgba(255, 255, 255, 0.8);
}

.space-stats {
  display: flex;
  gap: 40px;
  margin-bottom: 24px;
}

.space-stats span {
  font-size: 14px;
  color: #2c3e50;
  text-shadow: 0 1px 3px rgba(255, 255, 255, 0.8);
}

.space-stats strong {
  font-size: 20px;
  margin-right: 6px;
  color: #2c3e50;
}

.edit-space-btn {
  background: linear-gradient(135deg, var(--theme-color), var(--theme-color)) !important;
  border: none !important;
  color: #fff !important;
  font-weight: 600;
  padding: 12px 24px;
  font-size: 15px;
  box-shadow: 0 4px 15px rgba(244, 63, 94, 0.3);
  filter: brightness(1);
}

.edit-space-btn:hover {
  filter: brightness(0.9);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(244, 63, 94, 0.4);
}

.space-content :deep(.el-tabs__header) {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px 16px 0 0;
  padding: 0 24px;
  box-shadow: 0 -4px 30px rgba(102, 126, 234, 0.2);
  margin-bottom: 0;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.space-content :deep(.el-tabs__content) {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 0 0 16px 16px;
  padding: 24px;
  box-shadow: 0 4px 30px rgba(102, 126, 234, 0.15);
  margin-top: 0;
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-top: none;
}

.article-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.announcement-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95) 0%, rgba(255, 255, 255, 0.9) 100%);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px rgba(102, 126, 234, 0.15);
}

.announcement-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.announcement-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--theme-color);
}

.announcement-content {
  color: #475569;
  line-height: 1.6;
  white-space: pre-wrap;
}

.pinned-section {
  margin-bottom: 30px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #2d3436;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid var(--theme-color);
  opacity: 0.3;
}

.section-title .el-icon {
  color: var(--theme-color);
}

/* 单栏布局 */
.article-list.layout-single {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 双栏布局 */
.article-list.layout-double {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

/* 卡片式布局（默认） */
.article-list.layout-card {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: #94a3b8;
}

.empty-icon {
  font-size: 80px;
  color: #cbd5e1;
  margin-bottom: 20px;
}

.empty-text {
  font-size: 16px;
  margin-bottom: 24px;
  color: #64748b;
}

.empty-state .el-button {
  background: var(--theme-color);
  border-color: var(--theme-color);
}

.empty-state .el-button:hover {
  filter: brightness(0.9);
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.user-item:hover {
  background: #fff8f5;
  border-color: #ffe4d9;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-info .name {
  font-weight: 600;
  color: #2d3436;
}

.user-info .bio {
  color: #636e72;
  font-size: 13px;
  margin-top: 4px;
}

.author-actions {
  display: flex;
  gap: 10px;
  margin-top: 14px;
}

/* 背景上传样式 */
.bg-uploader :deep(.el-upload) {
  width: 100%;
  border: 2px dashed #d9d9d9;
  border-radius: 12px;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s;
}

.bg-uploader :deep(.el-upload:hover) {
  border-color: var(--theme-color);
}

.bg-uploader :deep(.el-upload-dragger) {
  width: 100%;
  height: 280px;
  border: none;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
}

.upload-icon {
  font-size: 60px;
  color: #d9d9d9;
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #606266;
  margin-bottom: 8px;
}

.upload-tip {
  font-size: 13px;
  color: #909399;
}

.bg-preview {
  position: relative;
  width: 100%;
  height: 280px;
}

.bg-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

.preview-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 8px;
}

.bg-preview:hover .preview-mask {
  opacity: 1;
}

.preview-mask .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.preview-mask span {
  font-size: 14px;
}
</style>
