<template>
  <div class="home-page">
    <!-- 轮播图 -->
    <Carousel />
    
    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 左侧内容 -->
      <div class="content-left">
        <!-- 筛选排序栏 -->
        <FilterBar @filter-change="handleFilterChange" />
        
        <!-- 文章列表 -->
        <div class="article-list">
          <!-- 骨架屏加载 -->
          <SkeletonList v-if="loading" :count="pageSize" />
          
          <!-- 实际文章列表 -->
          <template v-else>
            <ArticleCard 
              v-for="article in articles" 
              :key="article.id" 
              :article="article" 
            />
            
            <!-- 空状态 -->
            <el-empty v-if="!articles.length" description="暂无文章" />
          </template>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-wrapper">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 30, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
      
      <!-- 右侧边栏 -->
      <div class="sidebar">
        <!-- 分类列表 -->
        <div class="sidebar-card">
          <h3 class="sidebar-title">
            <el-icon><Folder /></el-icon>
            文章分类
          </h3>
          <div class="category-list">
            <div 
              v-for="cat in categories" 
              :key="cat.id" 
              :class="['category-item', { active: selectedCategory === cat.id }]"
              @click="selectCategory(cat.id)"
            >
              <span class="cat-name">{{ cat.name }}</span>
              <span class="cat-count">{{ cat.count }}</span>
            </div>
          </div>
        </div>
        
        <!-- 热门标签 -->
        <div class="sidebar-card">
          <h3 class="sidebar-title">
            <el-icon><PriceTag /></el-icon>
            热门标签
          </h3>
          <div class="tag-cloud">
            <el-tag 
              v-for="tag in hotTags" 
              :key="tag.id"
              :class="{ active: selectedTag === tag.id }"
              @click="selectTag(tag.id)"
            >
              {{ tag.name }}
            </el-tag>
          </div>
        </div>
        
        <!-- 热门作者 -->
        <div class="sidebar-card">
          <h3 class="sidebar-title">
            <el-icon><User /></el-icon>
            热门博主
          </h3>
          <div class="author-list">
            <div 
              v-for="author in hotAuthors" 
              :key="author.id" 
              class="author-item"
              @click="$router.push(`/space/${author.id}`)"
            >
              <el-avatar :size="40" :src="author.avatar" />
              <div class="author-info">
                <span class="author-name">{{ author.nickname }}</span>
                <span class="author-articles">{{ author.articleCount }} 篇文章</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 回到顶端按钮 -->
    <BackToTop />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useArticleStore } from '@/stores/article'
import { Folder, PriceTag, User } from '@element-plus/icons-vue'
import Carousel from '@/components/Carousel.vue'
import FilterBar from '@/components/FilterBar.vue'
import ArticleCard from '@/components/ArticleCard.vue'
import SkeletonList from '@/components/SkeletonList.vue'
import BackToTop from '@/components/BackToTop.vue'

const articleStore = useArticleStore()

const articles = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedCategory = ref(null)
const selectedTag = ref(null)

// 分类数据
const categories = ref([])

// 加载分类列表
const loadCategories = async () => {
  try {
    const { getCategories } = await import('@/api/article')
    const response = await getCategories()
    categories.value = (response || []).map(cat => ({
      id: cat.id,
      name: cat.name,
      count: cat.count || 0 // 使用API返回的文章数量
    })).sort((a, b) => b.count - a.count) // 按文章数量降序排序
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 热门标签
const hotTags = ref([])

// 加载标签列表
const loadTags = async () => {
  try {
    const { getTags } = await import('@/api/article')
    const response = await getTags()
    hotTags.value = (response || []).map(tag => ({
      id: tag.id,
      name: tag.name
    }))
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

// 热门博主
const hotAuthors = ref([
  { id: '3', nickname: '胡雪岩', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan', articleCount: 10 },
  { id: '2', nickname: '左宗棠', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zuozongtang', articleCount: 5 },
  { id: '5', nickname: '盛宣怀', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=shengxuanhuai', articleCount: 6 },
  { id: '4', nickname: '张謇', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangjian', articleCount: 4 }
])

// 模拟文章数据
const mockArticles = [
  {
    id: 1,
    title: '胡雪岩的商业智慧：从学徒到红顶商人',
    summary: '探讨胡雪岩如何从一个钱庄学徒成长为清朝首富的传奇经历，以及他的经商之道对现代商业的启示。',
    coverImage: 'https://picsum.photos/800/400?random=10',
    authorId: '3',
    authorName: '胡雪岩',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan',
    viewCount: 18680,
    likeCount: 2340,
    favoriteCount: 1560,
    commentCount: 156,
    isPaid: true,
    price: 29.9,
    categoryId: 6,
    categoryName: '历史文化',
    tags: ['胡雪岩', '徽商', '商业智慧'],
    createTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 2,
    title: '徽商精神：诚信为本，以义取利',
    summary: '徽商文化的核心价值观及其对现代商业的启示...',
    coverImage: 'https://picsum.photos/800/400?random=11',
    authorId: '3',
    authorName: '胡雪岩',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan',
    viewCount: 12450,
    likeCount: 1890,
    favoriteCount: 980,
    commentCount: 98,
    isPaid: false,
    categoryId: 6,
    categoryName: '历史文化',
    tags: ['徽商文化', '商业伦理', '传统文化'],
    createTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 3,
    title: 'Vue 3 Composition API 实战指南',
    summary: '深入讲解 Vue 3 Composition API 的使用技巧和最佳实践',
    coverImage: 'https://picsum.photos/800/400?random=30',
    authorId: '3',
    authorName: '胡雪岩',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan',
    viewCount: 25680,
    likeCount: 3140,
    favoriteCount: 2360,
    commentCount: 186,
    isPaid: true,
    price: 19.9,
    categoryId: 1,
    categoryName: '技术开发',
    tags: ['前端开发', 'Vue.js'],
    createTime: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 4,
    title: 'Spring Boot 微服务架构设计',
    summary: '分享微服务架构的设计原则和实践经验',
    coverImage: 'https://picsum.photos/800/400?random=31',
    authorId: '2',
    authorName: '左宗棠',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zuozongtang',
    viewCount: 21450,
    likeCount: 2790,
    favoriteCount: 1980,
    commentCount: 128,
    isPaid: false,
    categoryId: 1,
    categoryName: '技术开发',
    tags: ['Java', 'Spring Boot', '微服务'],
    createTime: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 5,
    title: 'Python 数据分析实战',
    summary: '使用 Python 进行数据分析的完整教程',
    coverImage: 'https://picsum.photos/800/400?random=32',
    authorId: '3',
    authorName: '盛宣怀',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=shengxuanhuai',
    viewCount: 19230,
    likeCount: 2560,
    favoriteCount: 1780,
    commentCount: 95,
    isPaid: false,
    categoryId: 1,
    categoryName: '技术开发',
    tags: ['Python', 'AI人工智能'],
    createTime: new Date(Date.now() - 12 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 6,
    title: 'MySQL 性能优化技巧',
    summary: '分享 MySQL 数据库性能优化的实用技巧',
    coverImage: 'https://picsum.photos/800/400?random=33',
    authorId: '4',
    authorName: '张謇',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangjian',
    viewCount: 16870,
    likeCount: 2340,
    favoriteCount: 1570,
    commentCount: 76,
    isPaid: false,
    categoryId: 1,
    categoryName: '技术开发',
    tags: ['数据库'],
    createTime: new Date(Date.now() - 18 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 7,
    title: 'AI 人工智能入门指南',
    summary: '人工智能基础知识和入门路径',
    coverImage: 'https://picsum.photos/800/400?random=34',
    authorId: '5',
    authorName: '李鸿章',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=lihongzhang',
    viewCount: 28900,
    likeCount: 3560,
    favoriteCount: 2780,
    commentCount: 210,
    isPaid: true,
    price: 39.9,
    categoryId: 1,
    categoryName: '技术开发',
    tags: ['AI人工智能', 'Python'],
    createTime: new Date(Date.now() - 3 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 8,
    title: '人生感悟：成功的真谛',
    summary: '分享对成功的理解和感悟',
    coverImage: 'https://picsum.photos/800/400?random=40',
    authorId: '6',
    authorName: '曾国藩',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zengguofan',
    viewCount: 14760,
    likeCount: 1930,
    favoriteCount: 1260,
    commentCount: 87,
    isPaid: false,
    categoryId: 2,
    categoryName: '生活感悟',
    tags: ['程序人生'],
    createTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 9,
    title: '读书与修身',
    summary: '谈谈读书对个人修养的重要性',
    coverImage: 'https://picsum.photos/800/400?random=41',
    authorId: '6',
    authorName: '曾国藩',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zengguofan',
    viewCount: 12650,
    likeCount: 1720,
    favoriteCount: 1080,
    commentCount: 72,
    isPaid: false,
    categoryId: 2,
    categoryName: '生活感悟',
    tags: ['读书'],
    createTime: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 10,
    title: '传统养生之道',
    summary: '介绍中国传统养生方法',
    coverImage: 'https://picsum.photos/800/400?random=50',
    authorId: '7',
    authorName: '林则徐',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=linzexu',
    viewCount: 10540,
    likeCount: 1480,
    favoriteCount: 860,
    commentCount: 54,
    isPaid: false,
    categoryId: 4,
    categoryName: '健康养生',
    tags: ['健康'],
    createTime: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 11,
    title: '收复新疆：一场艰苦卓绝的战争',
    summary: '回顾左宗棠西征收复新疆的历史过程...',
    coverImage: 'https://picsum.photos/800/400?random=20',
    authorId: '2',
    authorName: '左宗棠',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zuozongtang',
    viewCount: 12340,
    likeCount: 1890,
    favoriteCount: 1120,
    commentCount: 145,
    isPaid: false,
    categoryId: 6,
    categoryName: '历史文化',
    tags: ['历史', '军事', '新疆'],
    createTime: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 12,
    title: '洋务运动中的福建船政局',
    summary: '探讨福建船政局在洋务运动中的重要作用...',
    coverImage: 'https://picsum.photos/800/400?random=21',
    authorId: '2',
    authorName: '左宗棠',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zuozongtang',
    viewCount: 9870,
    likeCount: 1450,
    favoriteCount: 890,
    commentCount: 98,
    isPaid: false,
    categoryId: 6,
    categoryName: '历史文化',
    tags: ['洋务运动', '近代化', '海军'],
    createTime: new Date(Date.now() - 4 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 13,
    title: '轮船招商局的创办历程',
    summary: '讲述中国第一家轮船公司的创办故事...',
    coverImage: 'https://picsum.photos/800/400?random=22',
    authorId: '3',
    authorName: '盛宣怀',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=shengxuanhuai',
    viewCount: 8760,
    likeCount: 1230,
    favoriteCount: 760,
    commentCount: 87,
    isPaid: false,
    categoryId: 6,
    categoryName: '历史文化',
    tags: ['洋务运动', '航运', '近代企业'],
    createTime: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 14,
    title: '创办北洋大学堂的初衷',
    summary: '分享创办中国第一所现代大学的经历...',
    coverImage: 'https://picsum.photos/800/400?random=23',
    authorId: '3',
    authorName: '盛宣怀',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=shengxuanhuai',
    viewCount: 7890,
    likeCount: 1120,
    favoriteCount: 670,
    commentCount: 76,
    isPaid: false,
    categoryId: 2,
    categoryName: '生活感悟',
    tags: ['教育', '大学', '近代化'],
    createTime: new Date(Date.now() - 6 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 15,
    title: '大生纱厂的创办与发展',
    summary: '讲述中国近代民族工业的典范企业...',
    coverImage: 'https://picsum.photos/800/400?random=24',
    authorId: '4',
    authorName: '张謇',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangjian',
    viewCount: 6780,
    likeCount: 980,
    favoriteCount: 560,
    commentCount: 65,
    isPaid: false,
    categoryId: 3,
    categoryName: '职场经验',
    tags: ['实业', '纺织', '民族工业'],
    createTime: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 16,
    title: '状元办实业的心路历程',
    summary: '从科举状元到实业家的转变...',
    coverImage: 'https://picsum.photos/800/400?random=25',
    authorId: '4',
    authorName: '张謇',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhangjian',
    viewCount: 5890,
    likeCount: 870,
    favoriteCount: 490,
    commentCount: 54,
    isPaid: false,
    categoryId: 3,
    categoryName: '职场经验',
    tags: ['人物', '实业救国', '转型'],
    createTime: new Date(Date.now() - 8 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 17,
    title: '运动与健康',
    summary: '探讨运动对健康的重要性',
    coverImage: 'https://picsum.photos/800/400?random=51',
    authorId: '7',
    authorName: '林则徐',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=linzexu',
    viewCount: 8430,
    likeCount: 1270,
    favoriteCount: 790,
    commentCount: 45,
    isPaid: false,
    categoryId: 4,
    categoryName: '健康养生',
    tags: ['健康', '运动'],
    createTime: new Date(Date.now() - 4 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 18,
    title: '《资治通鉴》读书笔记',
    summary: '分享阅读《资治通鉴》的心得',
    coverImage: 'https://picsum.photos/800/400?random=60',
    authorId: '8',
    authorName: '康有为',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=kangyw',
    viewCount: 7320,
    likeCount: 1060,
    favoriteCount: 620,
    commentCount: 38,
    isPaid: false,
    categoryId: 5,
    categoryName: '读书笔记',
    tags: ['读书', '历史'],
    createTime: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 19,
    title: '《论语》的现代启示',
    summary: '从现代视角解读《论语》',
    coverImage: 'https://picsum.photos/800/400?random=61',
    authorId: '8',
    authorName: '康有为',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=kangyw',
    viewCount: 6890,
    likeCount: 970,
    favoriteCount: 580,
    commentCount: 32,
    isPaid: false,
    categoryId: 5,
    categoryName: '读书笔记',
    tags: ['读书', '传统文化'],
    createTime: new Date(Date.now() - 6 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  },
  {
    id: 20,
    title: '职场沟通的艺术',
    summary: '分享职场沟通的技巧和经验',
    coverImage: 'https://picsum.photos/800/400?random=70',
    authorId: '5',
    authorName: '李鸿章',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=lihongzhang',
    viewCount: 11230,
    likeCount: 1650,
    favoriteCount: 980,
    commentCount: 92,
    isPaid: false,
    categoryId: 3,
    categoryName: '职场经验',
    tags: ['职场', '沟通'],
    createTime: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]
  }
]

// 当前排序方式和时间范围
const currentSort = ref('latest')
const currentTimeRange = ref('all')

// 选择分类
const selectCategory = (catId) => {
  selectedCategory.value = selectedCategory.value === catId ? null : catId
  selectedTag.value = null // 清除标签筛选
  currentPage.value = 1 // 重置到第一页
  loadArticles()
}

// 选择标签
const selectTag = (tagId) => {
  selectedTag.value = selectedTag.value === tagId ? null : tagId
  selectedCategory.value = null // 清除分类筛选
  currentPage.value = 1 // 重置到第一页
  loadArticles()
}

// 处理筛选变化
const handleFilterChange = (filters) => {
  selectedCategory.value = filters.categoryId
  selectedTag.value = filters.tagId
  currentSort.value = filters.sortBy
  currentTimeRange.value = filters.timeRange
  loadArticles()
}

// 加载文章列表
const loadArticles = async () => {
  loading.value = true
  try {
    // 调用API获取文章列表
    const { getArticles } = await import('@/api/article')
    const params = {
      categoryId: selectedCategory.value,
      tagId: selectedTag.value, // 添加标签筛选参数
      page: currentPage.value - 1, // 后端分页从0开始
      size: pageSize.value,
      sortBy: currentSort.value === 'latest' ? 'createdAt' : 
              currentSort.value === 'hottest' ? 'viewCount' : 
              currentSort.value === 'mostLiked' ? 'favoriteCount' :
              'createdAt' // 默认按创建时间降序
    }
    
    console.log('加载文章 - 当前排序:', currentSort.value, '排序参数:', params.sortBy)
    
    const response = await getArticles(params)
    
    // 响应拦截器已经解包data，response就是分页数据
    if (response && response.content) {
      articles.value = response.content.map(article => ({
        id: article.id,
        title: article.title,
        summary: article.summary || article.content?.substring(0, 100) + '...',
        coverImage: article.coverImage,
        authorId: article.author?.id,
        authorName: article.author?.nickname || '未知作者',
        authorAvatar: article.author?.avatar,
        viewCount: article.viewCount || 0,
        likeCount: article.likeCount || 0,
        favoriteCount: article.favoriteCount || 0,
        commentCount: article.commentCount || 0,
        isPaid: article.isPaid,
        price: article.price,
        categoryId: article.categoryId,
        categoryName: article.categoryName,
        tags: article.tags || [],
        createTime: article.createdAt
      }))
      total.value = response.totalElements || 0
    } else {
      articles.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('加载文章失败:', error)
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadArticles()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadArticles()
}

onMounted(() => {
  loadCategories()
  loadTags()
  loadArticles()
})
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
}

.main-container {
  display: flex;
  gap: 24px;
  margin-top: 24px;
}

.content-left {
  flex: 1;
  min-width: 0;
}

.sidebar {
  width: 280px;
  flex-shrink: 0;
}

.article-list {
  margin-top: 20px;
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 36px;
  padding-bottom: 36px;
}

/* 侧边栏卡片 */
.sidebar-card {
  background: #fff;
  border-radius: 16px;
  padding: 18px;
  margin-bottom: 18px;
  box-shadow: 0 2px 12px rgba(244, 63, 94, 0.08);
  border: 1px solid #fecdd3;
}

.sidebar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 2px solid #ffe4e6;
}

.sidebar-title .el-icon {
  color: #f43f5e;
}

/* 分类列表 */
.category-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  background: transparent;
}

.category-item:hover {
  background: #ffe4e6;
}

.category-item.active {
  background: linear-gradient(135deg, #f43f5e, #fb7185);
  color: #fff;
}

.cat-name {
  font-size: 13px;
  color: #4b5563;
}

.category-item.active .cat-name {
  color: #fff;
  font-weight: 500;
}

.cat-count {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  background: #ffe4e6;
  color: #f43f5e;
}

.category-item.active .cat-count {
  background: rgba(255, 255, 255, 0.3);
  color: #fff;
}

/* 标签云 */
.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-cloud .el-tag {
  cursor: pointer;
  transition: all 0.2s;
  background: #ffe4e6;
  border-color: #fecdd3;
  color: #e11d48;
  border-radius: 16px;
  padding: 4px 12px;
  font-size: 12px;
}

.tag-cloud .el-tag:hover {
  background: #fecdd3;
}

.tag-cloud .el-tag.active {
  background: linear-gradient(135deg, #f43f5e, #ec4899);
  border-color: transparent;
  color: #fff;
}

/* 热门博主 */
.author-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.author-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.author-item:hover {
  background: #ffe4e6;
}

.author-info {
  display: flex;
  flex-direction: column;
}

.author-info .author-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.author-info .author-articles {
  font-size: 12px;
  color: #9ca3af;
}

/* 响应式 */
@media (max-width: 900px) {
  .main-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
  }
  
  .article-list {
    grid-template-columns: 1fr;
  }
}
</style>
