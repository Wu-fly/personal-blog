<template>
  <div id="app" :class="{ 'no-layout': noLayout }">
    <template v-if="!noLayout">
      <Header />
      <main class="main-content">
        <router-view />
      </main>
      <Footer :transparent="isPersonalSpace" />
    </template>
    <template v-else>
      <router-view />
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Header from '@/components/Header.vue'
import Footer from '@/components/Footer.vue'

const route = useRoute()

// 登录、注册页面不显示头部和底部
const noLayout = computed(() => {
  return ['Login', 'Register'].includes(route.name)
})

// 个人空间页面使用透明Footer
const isPersonalSpace = computed(() => {
  return route.path.startsWith('/space/')
})
</script>

<style>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #faf8f6;
}

#app.no-layout {
  display: block;
}

.main-content {
  flex: 1;
  padding-top: 84px;
  background: linear-gradient(180deg, #fff8f5 0%, #faf8f6 100%);
}
</style>
