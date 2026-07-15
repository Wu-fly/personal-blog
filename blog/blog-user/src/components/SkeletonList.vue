<template>
  <div class="skeleton-list">
    <SkeletonCard v-for="i in count" :key="i" />
  </div>
</template>

<script setup>
import SkeletonCard from './SkeletonCard.vue'

defineProps({
  count: {
    type: Number,
    default: 3
  }
})
</script>

<style scoped>
.skeleton-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 20px 0;
}

@media (max-width: 768px) {
  .skeleton-list {
    grid-template-columns: 1fr;
  }
}
</style>
