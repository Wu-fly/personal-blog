<template>
  <div class="dashboard">
    <h1>仪表盘</h1>
    <p>欢迎来到博客管理后台</p>
    
    <el-row :gutter="20" v-loading="loading">
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #409EFF;">
              <el-icon size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.userCount || 0 }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #67C23A;">
              <el-icon size="32"><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.articleCount || 0 }}</div>
              <div class="stat-label">文章总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #E6A23C;">
              <el-icon size="32"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.pendingArticleCount || 0 }}</div>
              <div class="stat-label">待审核文章</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card>
          <div class="stat-card">
            <div class="stat-icon" style="background-color: #F56C6C;">
              <el-icon size="32"><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ (statistics.totalRevenue || 0).toFixed(2) }}</div>
              <div class="stat-label">平台收益</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>待处理事项</span>
            </div>
          </template>
          <el-empty v-if="!pendingItems.length" description="暂无待处理事项" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="item in pendingItems"
              :key="item.id"
              :timestamp="item.time"
              placement="top"
            >
              <el-card>
                <h4>{{ item.title }}</h4>
                <p>{{ item.description }}</p>
                <el-button size="small" type="primary" @click="handlePendingItem(item)">
                  去处理
                </el-button>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/articles/review')">
              <el-icon><Document /></el-icon>
              文章审核
            </el-button>
            <el-button type="success" @click="$router.push('/blogger-applications')">
              <el-icon><User /></el-icon>
              博主申请
            </el-button>
            <el-button type="warning" @click="$router.push('/users')">
              <el-icon><Setting /></el-icon>
              用户管理
            </el-button>
            <el-button type="info" @click="$router.push('/carousel')">
              <el-icon><Picture /></el-icon>
              轮播图管理
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据可视化图表 -->
    <el-row :gutter="20" style="margin-top: 20px;" v-loading="chartLoading">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>文章发布趋势</span>
            </div>
          </template>
          <LineChart :data="articleTrendData" height="300px" />
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>用户增长趋势</span>
            </div>
          </template>
          <LineChart :data="userGrowthData" height="300px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;" v-loading="chartLoading">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>平台收益趋势</span>
            </div>
          </template>
          <LineChart :data="revenueTrendData" height="300px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>文章分类分布</span>
            </div>
          </template>
          <PieChart :data="categoryData" title="分类分布" height="350px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '../stores/admin'
import { User, Document, Warning, Wallet, Setting, Picture } from '@element-plus/icons-vue'
import { getStatistics, getPendingArticles, getBloggerApplications, getChartData } from '../api/admin'
import { ElMessage } from 'element-plus'
import LineChart from '../components/charts/LineChart.vue'
import PieChart from '../components/charts/PieChart.vue'

const router = useRouter()
const loading = ref(false)
const chartLoading = ref(false)
const statistics = ref({})
const pendingItems = ref([])

// 图表数据
const articleTrendData = ref({
  legend: ['文章发布数'],
  xAxis: [],
  series: []
})

const userGrowthData = ref({
  legend: ['用户注册数'],
  xAxis: [],
  series: []
})

const revenueTrendData = ref({
  legend: ['收入', '支出', '净收益'],
  xAxis: [],
  series: []
})

const categoryData = ref([])

// 加载统计数据
async function loadStatistics() {
  try {
    loading.value = true
    const res = await getStatistics()
    if (res.success && res.data) {
      statistics.value = res.data
    } else if (res.code === 200 && res.data) {
      statistics.value = res.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 即使失败也显示默认值，不显示错误提示
    statistics.value = {
      userCount: 0,
      articleCount: 0,
      pendingArticleCount: 0,
      totalRevenue: 0
    }
  } finally {
    loading.value = false
  }
}

// 加载图表数据
async function loadChartData() {
  try {
    chartLoading.value = true
    const res = await getChartData()
    if ((res.success || res.code === 200) && res.data) {
      const data = res.data
      
      // 文章趋势数据
      if (data.articleTrend) {
        articleTrendData.value = {
          legend: ['文章发布数'],
          xAxis: data.articleTrend.dates || [],
          series: [{
            name: '文章发布数',
            type: 'line',
            smooth: true,
            data: data.articleTrend.values || [],
            itemStyle: {
              color: '#67C23A'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: 'rgba(103, 194, 58, 0.3)'
                }, {
                  offset: 1, color: 'rgba(103, 194, 58, 0.05)'
                }]
              }
            }
          }]
        }
      } else {
        // 提供默认空数据
        articleTrendData.value = {
          legend: [],
          xAxis: [],
          series: []
        }
      }
      
      // 用户增长数据
      if (data.userGrowth) {
        userGrowthData.value = {
          legend: ['用户注册数'],
          xAxis: data.userGrowth.dates || [],
          series: [{
            name: '用户注册数',
            type: 'line',
            smooth: true,
            data: data.userGrowth.values || [],
            itemStyle: {
              color: '#409EFF'
            },
            areaStyle: {
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: 'rgba(64, 158, 255, 0.3)'
                }, {
                  offset: 1, color: 'rgba(64, 158, 255, 0.05)'
                }]
              }
            }
          }]
        }
      } else {
        // 提供默认空数据
        userGrowthData.value = {
          legend: [],
          xAxis: [],
          series: []
        }
      }
      
      // 分类分布数据
      if (data.categoryDistribution) {
        categoryData.value = data.categoryDistribution.map(item => ({
          name: item.name,
          value: item.value
        }))
      }
      
      // 平台收益趋势数据
      if (data.revenueTrend) {
        revenueTrendData.value = {
          legend: ['收入', '支出', '净收益'],
          xAxis: data.revenueTrend.dates || [],
          series: [
            {
              name: '收入',
              type: 'line',
              smooth: true,
              data: data.revenueTrend.income || [],
              itemStyle: {
                color: '#67C23A'
              }
            },
            {
              name: '支出',
              type: 'line',
              smooth: true,
              data: data.revenueTrend.expense || [],
              itemStyle: {
                color: '#F56C6C'
              }
            },
            {
              name: '净收益',
              type: 'line',
              smooth: true,
              data: data.revenueTrend.profit || [],
              itemStyle: {
                color: '#409EFF'
              },
              areaStyle: {
                color: {
                  type: 'linear',
                  x: 0,
                  y: 0,
                  x2: 0,
                  y2: 1,
                  colorStops: [{
                    offset: 0, color: 'rgba(64, 158, 255, 0.3)'
                  }, {
                    offset: 1, color: 'rgba(64, 158, 255, 0.05)'
                  }]
                }
              }
            }
          ]
        }
      } else {
        revenueTrendData.value = {
          legend: [],
          xAxis: [],
          series: []
        }
      }
    }
  } catch (error) {
    console.error('加载图表数据失败:', error)
  } finally {
    chartLoading.value = false
  }
}

// 加载待处理事项
async function loadPendingItems() {
  try {
    const items = []
    
    // 获取待审核文章
    try {
      const articlesRes = await getPendingArticles({ page: 0, size: 5 })
      if ((articlesRes.success || articlesRes.code === 200) && articlesRes.data) {
        // Spring Data Page 对象使用 content 字段
        const articles = articlesRes.data.content || articlesRes.data.list || []
        articles.forEach(article => {
          items.push({
            id: `article-${article.id}`,
            type: 'article',
            title: '待审核文章',
            description: article.title,
            time: article.createdAt,
            data: article
          })
        })
      }
    } catch (err) {
      console.error('获取待审核文章失败:', err)
    }

    // 获取待审核博主申请
    try {
      const applicationsRes = await getBloggerApplications({ status: 'PENDING', page: 0, size: 5 })
      if ((applicationsRes.success || applicationsRes.code === 200) && applicationsRes.data) {
        // Spring Data Page 对象使用 content 字段
        const applications = applicationsRes.data.content || applicationsRes.data.list || []
        applications.forEach(app => {
          items.push({
            id: `application-${app.id}`,
            type: 'application',
            title: '博主申请',
            description: `${app.nickname || app.user?.nickname || '用户'} 申请成为博主`,
            time: app.createdAt,
            data: app
          })
        })
      }
    } catch (err) {
      console.error('获取博主申请失败:', err)
    }

    // 按时间排序
    items.sort((a, b) => new Date(b.time) - new Date(a.time))
    pendingItems.value = items.slice(0, 10)
  } catch (error) {
    console.error('加载待处理事项失败:', error)
  }
}

// 处理待处理事项
function handlePendingItem(item) {
  if (item.type === 'article') {
    router.push('/articles/review')
  } else if (item.type === 'application') {
    router.push('/blogger-applications')
  }
}

onMounted(() => {
  const adminStore = useAdminStore()
  // 只有在已登录状态下才加载数据
  if (adminStore.isLoggedIn) {
    loadStatistics()
    loadPendingItems()
    loadChartData()
  }
})
</script>

<style scoped>
.dashboard h1 {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.quick-actions .el-button {
  width: 100%;
  justify-content: flex-start;
}
</style>
