<template>
  <div class="article-card" @click="goToDetail">
    <div class="card-cover" v-if="article.coverImage">
      <img :src="article.coverImage" :alt="article.title" />
      <el-tag v-if="showPinBadge && article.isPinned" type="danger" class="pin-badge">
        <el-icon><Star /></el-icon> 置顶
      </el-tag>
      <el-tag v-if="article.isPaid" type="warning" class="paid-tag">
        ¥{{ article.price }}
      </el-tag>
      <el-tag v-if="article.categoryName" class="category-tag">
        {{ article.categoryName }}
      </el-tag>
      <el-button 
        v-if="showUnpinAction && article.isPinned" 
        type="danger" 
        size="small" 
        class="unpin-btn"
        @click.stop="$emit('unpin', article)"
      >
        <el-icon><Close /></el-icon> 取消置顶
      </el-button>
      <el-button 
        v-if="showPinAction && !article.isPinned" 
        type="primary" 
        size="small" 
        class="pin-btn"
        @click.stop="$emit('pin', article)"
      >
        <el-icon><Star /></el-icon> 置顶
      </el-button>
      <el-button 
        v-if="showEditAction" 
        type="success" 
        size="small" 
        class="edit-btn"
        @click.stop="goToEdit"
      >
        <el-icon><Edit /></el-icon> 编辑
      </el-button>
    </div>
    <div class="card-content">
      <h3 class="card-title">{{ article.title }}</h3>
      <p class="card-summary">{{ article.summary }}</p>
      <div class="card-meta">
        <div class="author-info" @click.stop="goToSpace">
          <el-avatar :size="24" :src="article.authorAvatar" />
          <span class="author-name">{{ article.authorName }}</span>
        </div>
        <div class="stats">
          <span><el-icon><View /></el-icon> {{ formatCount(article.viewCount) }}</span>
          <span><el-icon><Star /></el-icon> {{ formatCount(article.likeCount) }}</span>
          <span><el-icon><ChatDotRound /></el-icon> {{ article.commentCount }}</span>
        </div>
      </div>
      <div class="card-footer">
        <span class="time">{{ article.createTime }}</span>
        <div class="tags">
          <el-tag v-for="tag in article.tags?.slice(0, 3)" :key="tag.id || tag" size="small" type="info">{{ typeof tag === 'object' ? tag.name : tag }}</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { View, Star, ChatDotRound, Edit, Close } from '@element-plus/icons-vue'

const props = defineProps({
  article: { type: Object, required: true },
  showPinBadge: { type: Boolean, default: false },
  showPinAction: { type: Boolean, default: false },
  showUnpinAction: { type: Boolean, default: false },
  showEditAction: { type: Boolean, default: false }
})

defineEmits(['pin', 'unpin'])

const router = useRouter()

const goToDetail = () => {
  router.push(`/article/${props.article.id}`)
}

const goToSpace = () => {
  router.push(`/space/${props.article.authorId}`)
}

const goToEdit = () => {
  router.push(`/article-editor/${props.article.id}`)
}

// 格式化数字
const formatCount = (count) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  } else if (count >= 1000) {
    return (count / 1000).toFixed(1) + 'k'
  }
  return count
}
</script>

<style scoped>
.article-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(244, 63, 94, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
  border: 1px solid var(--theme-color, #fecdd3);
  border-opacity: 0.3;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(244, 63, 94, 0.15);
  border-color: var(--theme-color, #fda4af);
  border-opacity: 0.5;
}

.card-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s;
}

.article-card:hover .card-cover img {
  transform: scale(1.05);
}

.pin-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: var(--theme-color, #f43f5e);
  border: none;
  color: #fff;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

.paid-tag {
  position: absolute;
  top: 12px;
  right: 12px;
  background: linear-gradient(135deg, #f59e0b, #f97316);
  border: none;
  color: #fff;
  font-weight: 600;
}

.pin-btn {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background: var(--theme-color, rgba(244, 63, 94, 0.9));
  border: none;
  color: #fff;
  font-weight: 500;
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0.9;
}

.pin-btn:hover {
  opacity: 1;
  filter: brightness(0.9);
}

.unpin-btn {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(239, 68, 68, 0.9);
  border: none;
  color: #fff;
  font-weight: 500;
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 10;
}

.unpin-btn:hover {
  background: rgba(220, 38, 38, 0.95);
}

.edit-btn {
  position: absolute;
  bottom: 12px;
  left: 12px;
  background: rgba(16, 185, 129, 0.9);
  border: none;
  color: #fff;
  font-weight: 500;
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  gap: 4px;
}

.edit-btn:hover {
  background: rgba(5, 150, 105, 0.95);
}

.category-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  color: var(--theme-color, #f43f5e);
  font-weight: 500;
  backdrop-filter: blur(4px);
}

.card-content {
  padding: 20px;
}

.card-title {
  font-size: 17px;
  font-weight: 600;
  margin-bottom: 10px;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;
}

.article-card:hover .card-title {
  color: var(--theme-color, #f43f5e);
}

.card-summary {
  color: #4b5563;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-size: 13px;
  color: #4b5563;
  font-weight: 500;
}

.stats {
  display: flex;
  gap: 14px;
  color: #9ca3af;
  font-size: 12px;
}

.stats span {
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
}

.stats span:hover {
  color: var(--theme-color, #f43f5e);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ffe4e6;
}

.time {
  color: #9ca3af;
  font-size: 12px;
}

.tags {
  display: flex;
  gap: 6px;
}

.tags .el-tag {
  background: #ffe4e6;
  border-color: var(--theme-color, #fecdd3);
  border-opacity: 0.3;
  color: var(--theme-color, #e11d48);
  font-size: 11px;
}
</style>
