<template>
  <div class="search-result-page">
    <div class="search-header">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文章..."
          size="large"
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" size="large" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
      
      <div class="search-info" v-if="searched">
        <span class="keyword">{{ currentKeyword }}</span>
        <span class="count">找到 {{ total }} 篇相关文章</span>
      </div>
    </div>

    <div class="search-content">
      <!-- 排序筛选栏 -->
      <div class="sort-bar" v-if="searched">
        <el-radio-group v-model="sortBy" @change="handleSortChange">
          <el-radio-button value="latest">最新发布</el-radio-button>
          <el-radio-button value="hottest">最多浏览</el-radio-button>
          <el-radio-button value="mostLiked">最多点赞</el-radio-button>
          <el-radio-button value="mostFavorited">最多收藏</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 搜索结果列表 -->
      <div v-if="articles.length > 0" class="result-list">
        <ArticleCard 
          v-for="article in articles" 
          :key="article.id" 
          :article="article" 
        />
      </div>

      <!-- 空状态 -->
      <el-empty 
        v-else-if="searched" 
        description="没有找到相关文章"
        :image-size="200"
      >
        <template #description>
          <p>没有找到与 "<span class="keyword">{{ currentKeyword }}</span>" 相关的文章</p>
          <p class="tip">试试其他关键词吧</p>
        </template>
      </el-empty>

      <!-- 未搜索状态 -->
      <div v-else class="search-tips">
        <el-icon :size="80" color="#f43f5e"><Search /></el-icon>
        <h3>输入关键词搜索文章</h3>
        <div class="hot-keywords">
          <span class="label">热门搜索：</span>
          <el-tag 
            v-for="keyword in hotKeywords" 
            :key="keyword"
            @click="searchByKeyword(keyword)"
            class="hot-tag"
          >
            {{ keyword }}
          </el-tag>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="articles.length > 0" class="pagination-wrapper">
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import ArticleCard from '@/components/ArticleCard.vue'

const route = useRoute()
const router = useRouter()

const searchKeyword = ref('')
const currentKeyword = ref('')
const searched = ref(false)
const articles = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const sortBy = ref('latest') // 排序方式

// 热门搜索关键词
const hotKeywords = ref([
  '胡雪岩', '徽商', '商业智慧', '洋务运动', 
  '实业救国', '近代化', 'Vue', 'Java'
])

// 模拟文章数据（与 Home.vue 相同）
const mockArticles = [
  {
    id: 1,
    title: '胡雪岩的商业智慧：从学徒到红顶商人',
    summary: '探讨胡雪岩如何从一个钱庄学徒成长为清朝首富的传奇经历，以及他的经商之道对现代商业的启示。',
    coverImage: 'https://picsum.photos/800/400?random=10',
    authorId: '1',
    authorName: '胡雪岩',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan',
    viewCount: 18680,
    likeCount: 2340,
    commentCount: 156,
    isPaid: true,
    price: 29.9,
    categoryId: 6,
    categoryName: '历史文化',
    tags: ['胡雪岩', '徽商', '商业智慧'],
    createTime: '2025-12-20'
  },
  {
    id: 2,
    title: '徽商精神：诚信为本，以义取利',
    summary: '徽商文化的核心价值观及其对现代商业的启示...',
    coverImage: 'https://picsum.photos/800/400?random=11',
    authorId: '1',
    authorName: '胡雪岩',
    authorAvatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=huxueyan',
    viewCount: 12450,
    likeCount: 1890,
    commentCount: 98,
    isPaid: false,
    categoryId: 6,
    categoryName: '历史文化',
    tags: ['徽商文化', '商业伦理', '传统文化'],
    createTime: '2025-12-18'
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
    commentCount: 145,
    isPaid: false,
    categoryId: 6,
    categoryName: '历史文化',
    tags: ['历史', '军事', '新疆'],
    createTime: '2025-12-17'
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
    commentCount: 98,
    isPaid: false,
    categoryId: 6,
    categoryName: '历史文化',
    tags: ['洋务运动', '近代化', '海军'],
    createTime: '2025-12-15'
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
    commentCount: 87,
    isPaid: false,
    categoryId: 6,
    categoryName: '历史文化',
    tags: ['洋务运动', '航运', '近代企业'],
    createTime: '2025-12-14'
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
    commentCount: 76,
    isPaid: false,
    categoryId: 2,
    categoryName: '生活感悟',
    tags: ['教育', '大学', '近代化'],
    createTime: '2025-12-12'
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
    commentCount: 65,
    isPaid: false,
    categoryId: 3,
    categoryName: '职场经验',
    tags: ['实业', '纺织', '民族工业'],
    createTime: '2025-12-10'
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
    commentCount: 54,
    isPaid: false,
    categoryId: 3,
    categoryName: '职场经验',
    tags: ['人物', '实业救国', '转型'],
    createTime: '2025-12-08'
  }
]

// 执行搜索
const performSearch = async (keyword) => {
  if (!keyword || !keyword.trim()) {
    return
  }

  currentKeyword.value = keyword.trim()
  searched.value = true

  try {
    // 调用搜索 API
    const { searchArticles } = await import('@/api/article')
    
    // 根据排序方式设置sortBy参数
    let sortField = 'createdAt'
    if (sortBy.value === 'hottest') {
      sortField = 'viewCount'
    } else if (sortBy.value === 'mostLiked') {
      sortField = 'likeCount'
    } else if (sortBy.value === 'mostFavorited') {
      sortField = 'favoriteCount'
    }
    
    const params = {
      keyword: keyword.trim(),
      page: currentPage.value - 1,
      size: pageSize.value,
      sortBy: sortField
    }
    
    const response = await searchArticles(keyword.trim(), params)
    
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
    console.error('搜索文章失败:', error)
    articles.value = []
    total.value = 0
  }
}

// 排序方式改变
const handleSortChange = () => {
  currentPage.value = 1
  performSearch(currentKeyword.value)
}

// 搜索按钮点击
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    return
  }
  
  // 更新 URL
  router.push({
    path: '/search',
    query: { q: searchKeyword.value.trim() }
  })
  
  performSearch(searchKeyword.value)
}

// 点击热门关键词搜索
const searchByKeyword = (keyword) => {
  searchKeyword.value = keyword
  handleSearch()
}

const handleSizeChange = (size) => {
  pageSize.value = size
  performSearch(currentKeyword.value)
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  performSearch(currentKeyword.value)
}

onMounted(() => {
  // 从 URL 获取搜索关键词
  const query = route.query.q
  if (query) {
    searchKeyword.value = query
    performSearch(query)
  }
})
</script>

<style scoped>
.search-result-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.search-header {
  margin-bottom: 30px;
}

.search-box {
  display: flex;
  gap: 12px;
  max-width: 800px;
  margin: 0 auto 20px;
}

.search-box .el-input {
  flex: 1;
}

.search-info {
  text-align: center;
  color: #6b7280;
  font-size: 14px;
}

.search-info .keyword {
  color: #f43f5e;
  font-weight: 600;
  margin-right: 10px;
}

.search-info .count {
  color: #9ca3af;
}

.search-content {
  min-height: 400px;
}

.sort-bar {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 14px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(244, 63, 94, 0.08);
  border: 1px solid #fecdd3;
  margin-bottom: 20px;
}

.sort-bar :deep(.el-radio-button__inner) {
  border-color: #fecdd3;
  font-size: 13px;
  padding: 8px 16px;
}

.sort-bar :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #f43f5e, #fb7185);
  border-color: #f43f5e;
  box-shadow: -1px 0 0 0 #f43f5e;
}

.result-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

.search-tips {
  text-align: center;
  padding: 80px 20px;
}

.search-tips h3 {
  margin: 20px 0 30px;
  color: #1f2937;
  font-size: 20px;
}

.hot-keywords {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 20px;
}

.hot-keywords .label {
  color: #6b7280;
  font-size: 14px;
}

.hot-tag {
  cursor: pointer;
  transition: all 0.2s;
  background: #ffe4e6;
  border-color: #fecdd3;
  color: #e11d48;
}

.hot-tag:hover {
  background: linear-gradient(135deg, #f43f5e, #ec4899);
  border-color: transparent;
  color: #fff;
  transform: translateY(-2px);
}

.keyword {
  color: #f43f5e;
  font-weight: 600;
}

.tip {
  color: #9ca3af;
  font-size: 14px;
  margin-top: 10px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding-bottom: 40px;
}

@media (max-width: 768px) {
  .search-box {
    flex-direction: column;
  }
}
</style>
