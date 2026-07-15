<template>
  <div class="carousel-container">
    <el-carousel v-if="carouselItems.length" :interval="5000" height="400px" indicator-position="outside">
      <el-carousel-item v-for="item in carouselItems" :key="item.id">
        <div class="carousel-item" :style="{ backgroundImage: `url(${item.coverImage})` }" @click="goToArticle(item.id)">
          <div class="carousel-overlay">
            <h3 class="carousel-title">{{ item.title }}</h3>
            <p class="carousel-desc">{{ item.summary }}</p>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const carouselItems = ref([])

const goToArticle = (id) => {
  router.push(`/article/${id}`)
}

const loadCarouselData = async () => {
  try {
    const response = await request({
      url: '/articles/carousel',
      method: 'get'
    })
    
    if (response && Array.isArray(response)) {
      carouselItems.value = response.map(config => ({
        id: config.article?.id,
        title: config.article?.title,
        summary: config.article?.summary || config.article?.content?.substring(0, 100) + '...',
        coverImage: config.article?.coverImage
      })).filter(item => item.id) // 过滤掉无效数据
    }
  } catch (error) {
    console.error('加载轮播图失败:', error)
  }
}

onMounted(() => {
  loadCarouselData()
})
</script>

<style scoped>
.carousel-container {
  margin-bottom: 30px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(255, 107, 74, 0.15);
}

.carousel-item {
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  cursor: pointer;
}

.carousel-overlay {
  width: 100%;
  height: 100%;
  background: linear-gradient(to top, rgba(0,0,0,0.6), transparent 60%);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 50px;
  color: #fff;
}

.carousel-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 12px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.3);
}

.carousel-desc {
  font-size: 16px;
  opacity: 0.95;
  text-shadow: 0 1px 5px rgba(0,0,0,0.2);
}
</style>
