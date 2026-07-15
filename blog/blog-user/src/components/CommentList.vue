<template>
  <div class="comment-list">
    <CommentItem 
      v-for="comment in comments" 
      :key="comment.id" 
      :comment="comment" 
      @reply="handleReply"
      @delete="handleDelete"
    />
    <el-empty v-if="!comments.length" description="暂无评论" />
  </div>
</template>

<script setup>
import CommentItem from './CommentItem.vue'

defineProps({
  comments: { type: Array, default: () => [] }
})

const emit = defineEmits(['reply', 'delete'])

const handleReply = (comment) => {
  emit('reply', comment)
}

const handleDelete = (commentId) => {
  emit('delete', commentId)
}
</script>

<style scoped>
.comment-list {
  margin-top: 20px;
}
</style>
