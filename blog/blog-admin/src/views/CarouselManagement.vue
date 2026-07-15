<template>
  <div class="carousel-management">
    <h1>轮播图管理</h1>
    
    <el-card>
      <template #header>
        <div class="card-header">
          <span>当前轮播图配置（最多5篇）</span>
          <el-button type="primary" @click="addArticleVisible = true">添加文章</el-button>
        </div>
      </template>

      <el-empty v-if="!carouselArticles.length" description="暂无轮播图配置" />
      
      <el-table v-else :data="carouselArticles" v-loading="loading" style="width: 100%">
        <el-table-column label="顺序" width="80">
          <template #default="{ row }">
            <el-tag>{{ row.displayOrder }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="封面" width="120">
          <template #default="{ row }">
            <el-image
              :src="row.coverImage"
              fit="cover"
              style="width: 100px; height: 60px; border-radius: 4px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="文章标题" min-width="200" />
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row, $index }">
            <el-button
              size="small"
              :disabled="$index === 0"
              @click="moveUp(row, $index)"
            >
              上移
            </el-button>
            <el-button
              size="small"
              :disabled="$index === carouselArticles.length - 1"
              @click="moveDown(row, $index)"
            >
              下移
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="removeArticle(row)"
            >
              移除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; text-align: center;">
        <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
      </div>
    </el-card>

    <!-- 添加文章对话框 -->
    <el-dialog
      v-model="addArticleVisible"
      title="添加轮播文章"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="文章标题"
            clearable
            @keyup.enter="searchArticles"
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-select v-model="searchForm.sortBy" placeholder="选择排序方式" @change="searchArticles" style="width: 150px;">
            <el-option label="最新发布" value="createdAt" />
            <el-option label="最多浏览" value="viewCount" />
            <el-option label="最多点赞" value="likeCount" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchArticles">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="availableArticles"
        v-loading="searchLoading"
        style="width: 100%"
        max-height="400"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              size="small"
              type="primary"
              :disabled="carouselArticles.length >= 5 || isInCarousel(row.id)"
              @click="addToCarousel(row)"
            >
              添加
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="searchPage"
        v-model:page-size="searchPageSize"
        :total="searchTotal"
        :page-sizes="[10, 20]"
        layout="total, sizes, prev, pager, next"
        @size-change="searchArticles"
        @current-change="searchArticles"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getCarouselConfig, updateCarouselConfig } from '../api/admin'
import { getArticleList } from '../api/article'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const saving = ref(false)
const carouselArticles = ref([])

const addArticleVisible = ref(false)
const searchLoading = ref(false)
const availableArticles = ref([])
const searchPage = ref(1)
const searchPageSize = ref(10)
const searchTotal = ref(0)
const searchForm = ref({
  keyword: '',
  sortBy: 'viewCount' // 默认按浏览量排序
})

// 监听对话框打开，自动加载文章
watch(addArticleVisible, (newVal) => {
  if (newVal) {
    searchArticles()
  }
})

// 加载轮播图配置
async function loadCarouselConfig() {
  try {
    loading.value = true
    const res = await getCarouselConfig()
    if (res && res.data) {
      // 后端返回的是CarouselConfig列表，需要获取完整的文章信息
      carouselArticles.value = (res.data || []).map(config => ({
        id: config.article?.id,
        title: config.article?.title,
        coverImage: config.article?.coverImage,
        authorName: config.article?.author?.nickname || '未知',
        viewCount: config.article?.viewCount || 0,
        displayOrder: config.displayOrder
      })).filter(item => item.id) // 过滤掉无效数据
    }
  } catch (error) {
    console.error('加载轮播图配置失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索文章
async function searchArticles() {
  try {
    searchLoading.value = true
    const res = await getArticleList({
      page: searchPage.value - 1, // 后端从0开始
      size: searchPageSize.value,
      keyword: searchForm.value.keyword,
      sortBy: searchForm.value.sortBy
    })
    
    console.log('搜索文章响应:', res)
    
    // 响应拦截器返回整个res对象，数据在res.data中
    if (res && res.data && res.data.content) {
      availableArticles.value = res.data.content.map(article => ({
        id: article.id,
        title: article.title,
        coverImage: article.coverImage,
        authorName: article.author?.nickname || article.author?.username || '未知',
        viewCount: article.viewCount || 0
      }))
      searchTotal.value = res.data.totalElements || 0
    } else {
      availableArticles.value = []
      searchTotal.value = 0
    }
  } catch (error) {
    console.error('搜索文章失败:', error)
    ElMessage.error('搜索文章失败: ' + (error.message || '未知错误'))
  } finally {
    searchLoading.value = false
  }
}

// 检查文章是否已在轮播图中
function isInCarousel(articleId) {
  return carouselArticles.value.some(item => item.id === articleId)
}

// 添加到轮播图
function addToCarousel(article) {
  if (carouselArticles.value.length >= 5) {
    ElMessage.warning('轮播图最多只能添加5篇文章')
    return
  }

  carouselArticles.value.push({
    ...article,
    displayOrder: carouselArticles.value.length + 1
  })

  ElMessage.success('已添加到轮播图')
}

// 上移
function moveUp(article, index) {
  if (index === 0) return
  
  const temp = carouselArticles.value[index]
  carouselArticles.value[index] = carouselArticles.value[index - 1]
  carouselArticles.value[index - 1] = temp
  
  updateDisplayOrder()
}

// 下移
function moveDown(article, index) {
  if (index === carouselArticles.value.length - 1) return
  
  const temp = carouselArticles.value[index]
  carouselArticles.value[index] = carouselArticles.value[index + 1]
  carouselArticles.value[index + 1] = temp
  
  updateDisplayOrder()
}

// 更新显示顺序
function updateDisplayOrder() {
  carouselArticles.value.forEach((item, index) => {
    item.displayOrder = index + 1
  })
}

// 移除文章
function removeArticle(article) {
  const index = carouselArticles.value.findIndex(item => item.id === article.id)
  if (index > -1) {
    carouselArticles.value.splice(index, 1)
    updateDisplayOrder()
    ElMessage.success('已移除')
  }
}

// 保存配置
async function saveConfig() {
  try {
    saving.value = true
    const config = carouselArticles.value.map(item => ({
      articleId: item.id,
      displayOrder: item.displayOrder
    }))

    await updateCarouselConfig(config)
    ElMessage.success('保存成功')
    await loadCarouselConfig()
  } catch (error) {
    console.error('保存配置失败:', error)
    ElMessage.error('保存配置失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadCarouselConfig()
})
</script>

<style scoped>
.carousel-management h1 {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}
</style>
