<template>
  <div class="filter-bar">
    <div class="filter-left">
      <!-- 分类筛选 -->
      <el-select v-model="filters.categoryId" placeholder="全部分类" clearable @change="handleFilterChange">
        <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
      </el-select>
      <!-- 标签筛选 -->
      <el-select v-model="filters.tagId" placeholder="全部标签" clearable @change="handleFilterChange">
        <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
      </el-select>
    </div>
    <div class="filter-right">
      <!-- 时间筛选 -->
      <el-select v-model="filters.timeRange" placeholder="时间范围" @change="handleFilterChange" style="width: 140px; margin-right: 12px;">
        <el-option label="全部时间" value="all" />
        <el-option label="24小时内" value="24h" />
        <el-option label="3天内" value="3d" />
        <el-option label="一周内" value="1w" />
        <el-option label="一周以上" value="1w+" />
      </el-select>
      <!-- 排序方式 -->
      <el-radio-group v-model="filters.sortBy" @change="handleFilterChange">
        <el-radio-button value="latest">最新发布</el-radio-button>
        <el-radio-button value="hottest">最多浏览</el-radio-button>
        <el-radio-button value="mostLiked">最多收藏</el-radio-button>
      </el-radio-group>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

const emit = defineEmits(['filter-change'])

const categories = ref([
  { id: 1, name: '技术开发' },
  { id: 2, name: '生活感悟' },
  { id: 3, name: '职场经验' },
  { id: 4, name: '健康养生' },
  { id: 5, name: '读书笔记' },
  { id: 6, name: '历史文化' }
])
const tags = ref([
  { id: 1, name: '前端' },
  { id: 2, name: 'Vue' },
  { id: 3, name: 'Java' },
  { id: 4, name: 'Spring Boot' },
  { id: 5, name: 'AI' },
  { id: 6, name: '健康' },
  { id: 7, name: '胡雪岩' },
  { id: 8, name: '微服务' }
])

const filters = reactive({
  categoryId: null,
  tagId: null,
  sortBy: 'latest',
  timeRange: 'all'
})

const handleFilterChange = () => {
  emit('filter-change', { ...filters })
}

onMounted(() => {
  // TODO: 加载分类和标签列表
})
</script>

<style scoped>
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(244, 63, 94, 0.08);
  border: 1px solid #fecdd3;
}

.filter-left {
  display: flex;
  gap: 12px;
}

.filter-right {
  display: flex;
  align-items: center;
}

.filter-right :deep(.el-radio-button__inner) {
  border-color: #fecdd3;
  font-size: 13px;
  padding: 8px 16px;
}

.filter-right :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #f43f5e, #fb7185);
  border-color: #f43f5e;
  box-shadow: -1px 0 0 0 #f43f5e;
}
</style>
