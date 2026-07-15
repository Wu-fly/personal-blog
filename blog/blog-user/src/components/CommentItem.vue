<template>
  <div class="comment-item">
    <el-avatar :size="40" :src="comment.avatar" />
    <div class="comment-content">
      <div class="comment-header">
        <span class="author">{{ comment.nickname }}</span>
        <span class="time">{{ comment.createTime }}</span>
      </div>
      <div class="comment-text">{{ comment.content }}</div>
      <div class="comment-actions">
        <span class="action" @click="handleLike">
          <el-icon><Star /></el-icon> {{ comment.likeCount || 0 }}
        </span>
        <span class="action" @click="handleReply">
          <el-icon><ChatDotRound /></el-icon> 回复
        </span>
        <!-- 只有评论作者才能删除 -->
        <span v-if="canDelete" class="action delete-action" @click="handleDelete">
          <el-icon><Delete /></el-icon> 删除
        </span>
      </div>
      <!-- 子评论 -->
      <div v-if="comment.replies?.length" class="replies">
        <CommentItem 
          v-for="reply in comment.replies" 
          :key="reply.id" 
          :comment="reply" 
          @reply="handleReply"
          @delete="handleDeleteReply"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessageBox, ElMessage } from 'element-plus'
import { Star, ChatDotRound, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  comment: { type: Object, required: true }
})

const emit = defineEmits(['reply', 'delete'])

const userStore = useUserStore()

// 判断当前用户是否可以删除该评论
const canDelete = computed(() => {
  if (!userStore.isLoggedIn) return false
  // 评论的 userId 字段与当前登录用户的 id 匹配
  return props.comment.userId === userStore.userInfo?.id
})

const handleLike = () => {
  // TODO: 点赞评论
}

const handleReply = () => {
  emit('reply', props.comment)
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    emit('delete', props.comment.id)
  } catch (error) {
    // 用户取消删除
  }
}

const handleDeleteReply = (commentId) => {
  emit('delete', commentId)
}
</script>

<style scoped>
.comment-item {
  display: flex;
  gap: 16px;
  padding: 20px 0;
  border-bottom: 1px solid #fff5f0;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.author {
  font-weight: 600;
  color: #2d3436;
}

.time {
  color: #b2bec3;
  font-size: 13px;
}

.comment-text {
  color: #636e72;
  line-height: 1.7;
  margin-bottom: 12px;
}

.comment-actions {
  display: flex;
  gap: 24px;
}

.action {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #b2bec3;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.3s;
}

.action:hover {
  color: #ff6b4a;
}

.delete-action:hover {
  color: #f43f5e;
}

.replies {
  margin-top: 16px;
  padding-left: 24px;
  border-left: 2px solid #ffe4d9;
}
</style>
