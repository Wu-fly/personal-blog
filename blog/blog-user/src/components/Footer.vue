<template>
  <footer class="app-footer" :class="{ 'transparent-mode': transparent }">
    <div class="footer-container">
      <div class="footer-content">
        <p class="copyright">© 2025 个人博客系统 All Rights Reserved</p>
        <div class="footer-links">
          <a href="#">关于我们</a>
          <a href="#">联系方式</a>
          <a href="#">隐私政策</a>
          <a href="#">使用条款</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
defineProps({
  transparent: {
    type: Boolean,
    default: false
  }
})
</script>

<style scoped>
.app-footer {
  background: linear-gradient(135deg, #2d3436 0%, #636e72 100%);
  color: #fff;
  padding: 40px 0;
  margin-top: auto;
}

.app-footer.transparent-mode {
  background: rgba(45, 52, 54, 0.8);
  backdrop-filter: blur(10px);
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-content {
  text-align: center;
}

.copyright {
  margin-bottom: 20px;
  color: #dfe6e9;
  font-size: 14px;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 40px;
}

.footer-links a {
  color: #dfe6e9;
  text-decoration: none;
  transition: color 0.3s;
  font-size: 14px;
}

.footer-links a:hover {
  color: #ffb347;
}
</style>
