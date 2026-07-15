<template>
  <div class="sheep-background">
    <!-- 背景图片 -->
    <div class="bg-image" :style="{ backgroundImage: `url(/${bgImage})` }"></div>
    
    <!-- 动态遮罩层 -->
    <div class="animated-overlay"></div>
    
    <!-- 飘动的云朵 -->
    <div class="floating-clouds">
      <div v-for="i in 5" :key="i" class="cloud" :style="getCloudStyle(i)"></div>
    </div>
    
    <!-- 闪烁的星星 -->
    <div class="twinkling-stars">
      <div v-for="i in 20" :key="i" class="star" :style="getStarStyle(i)"></div>
    </div>
    
    <!-- 光晕效果 -->
    <div class="light-spots">
      <div v-for="i in 8" :key="i" class="light-spot" :style="getLightSpotStyle(i)"></div>
    </div>
    
    <!-- 飘动的花瓣/叶子 -->
    <div class="floating-elements">
      <div v-for="i in 15" :key="i" class="floating-element" :style="getFloatingElementStyle(i)">
        <div class="element-inner"></div>
      </div>
    </div>
    
    <!-- 渐变光效 -->
    <div class="gradient-glow"></div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const bgImage = ref('sheep-bg.png')

const getCloudStyle = (index) => {
  const top = 10 + (index * 15) + (Math.random() * 10)
  const left = -20 + (index * 20)
  const scale = 0.5 + (Math.random() * 0.5)
  const duration = 40 + (Math.random() * 30)
  const delay = index * 3
  
  return {
    top: `${top}%`,
    left: `${left}%`,
    transform: `scale(${scale})`,
    animationDuration: `${duration}s`,
    animationDelay: `${delay}s`
  }
}

const getStarStyle = (index) => {
  const top = Math.random() * 60
  const left = Math.random() * 100
  const size = 2 + (Math.random() * 3)
  const delay = Math.random() * 5
  const duration = 2 + (Math.random() * 3)
  
  return {
    top: `${top}%`,
    left: `${left}%`,
    width: `${size}px`,
    height: `${size}px`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}

const getLightSpotStyle = (index) => {
  const top = 20 + (Math.random() * 60)
  const left = Math.random() * 100
  const size = 100 + (Math.random() * 150)
  const delay = Math.random() * 8
  const duration = 6 + (Math.random() * 4)
  
  return {
    top: `${top}%`,
    left: `${left}%`,
    width: `${size}px`,
    height: `${size}px`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}

const getFloatingElementStyle = (index) => {
  const left = Math.random() * 100
  const delay = Math.random() * 10
  const duration = 15 + (Math.random() * 10)
  const size = 6 + (Math.random() * 8)
  const hue = Math.random() * 60 + 300 // 粉色到紫色
  
  return {
    left: `${left}%`,
    width: `${size}px`,
    height: `${size}px`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`,
    '--element-color': `hsl(${hue}, 70%, 80%)`
  }
}
</script>

<style scoped>
.sheep-background {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  overflow: hidden;
}

/* 背景图片 */
.bg-image {
  position: absolute;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  animation: bgZoom 30s ease-in-out infinite;
}

@keyframes bgZoom {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* 动态遮罩层 */
.animated-overlay {
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.1) 0%,
    rgba(255, 255, 255, 0.05) 50%,
    rgba(255, 255, 255, 0.1) 100%
  );
  background-size: 200% 200%;
  animation: overlayShift 15s ease infinite;
}

@keyframes overlayShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 飘动的云朵 */
.floating-clouds {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.cloud {
  position: absolute;
  width: 150px;
  height: 50px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 100px;
  filter: blur(10px);
  animation: cloudFloat linear infinite;
}

.cloud::before,
.cloud::after {
  content: '';
  position: absolute;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
}

.cloud::before {
  width: 60px;
  height: 60px;
  top: -25px;
  left: 20px;
}

.cloud::after {
  width: 80px;
  height: 60px;
  top: -20px;
  right: 20px;
}

@keyframes cloudFloat {
  from { transform: translateX(-200px); }
  to { transform: translateX(calc(100vw + 200px)); }
}

/* 闪烁的星星 */
.twinkling-stars {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.star {
  position: absolute;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  animation: starTwinkle ease-in-out infinite;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.8);
}

@keyframes starTwinkle {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.3);
  }
}

/* 光晕效果 */
.light-spots {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.light-spot {
  position: absolute;
  background: radial-gradient(circle,
    rgba(255, 255, 255, 0.3) 0%,
    rgba(255, 255, 255, 0.1) 40%,
    transparent 70%
  );
  border-radius: 50%;
  animation: lightPulse ease-in-out infinite;
}

@keyframes lightPulse {
  0%, 100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.6;
    transform: scale(1.2);
  }
}

/* 飘动的元素 */
.floating-elements {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.floating-element {
  position: absolute;
  top: -20px;
  animation: elementFloat linear infinite;
}

.element-inner {
  width: 100%;
  height: 100%;
  background: var(--element-color, rgba(255, 182, 193, 0.7));
  border-radius: 50% 0 50% 0;
  box-shadow: 0 2px 8px rgba(255, 182, 193, 0.4);
  animation: elementRotate 4s linear infinite;
}

@keyframes elementFloat {
  0% {
    transform: translateY(0) translateX(0);
    opacity: 0;
  }
  10% {
    opacity: 0.8;
  }
  90% {
    opacity: 0.8;
  }
  100% {
    transform: translateY(100vh) translateX(50px);
    opacity: 0;
  }
}

@keyframes elementRotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 渐变光效 */
.gradient-glow {
  position: absolute;
  width: 100%;
  height: 100%;
  background: radial-gradient(
    ellipse at 30% 40%,
    rgba(255, 255, 255, 0.15) 0%,
    transparent 50%
  ),
  radial-gradient(
    ellipse at 70% 60%,
    rgba(255, 200, 220, 0.1) 0%,
    transparent 50%
  );
  animation: glowShift 20s ease-in-out infinite;
  pointer-events: none;
}

@keyframes glowShift {
  0%, 100% {
    opacity: 0.5;
    transform: scale(1);
  }
  50% {
    opacity: 0.8;
    transform: scale(1.1);
  }
}
</style>
