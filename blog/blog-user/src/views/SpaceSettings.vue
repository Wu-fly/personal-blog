<template>
  <div class="space-settings-page">
    <div class="settings-container">
      <h2>空间设置</h2>
      <el-form :model="settingsForm" label-width="100px" class="settings-form">
        <el-form-item label="头像">
          <el-upload class="avatar-uploader" action="#" :show-file-list="false" :before-upload="beforeAvatarUpload">
            <img v-if="settingsForm.avatar" :src="settingsForm.avatar" class="avatar-preview" />
            <el-icon v-else class="upload-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">建议尺寸：200x200像素</div>
        </el-form-item>
        
        <el-form-item label="背景图片">
          <el-upload class="cover-uploader" action="#" :show-file-list="false" :before-upload="beforeBgUpload">
            <img v-if="settingsForm.bgImage" :src="settingsForm.bgImage" class="cover-preview" />
            <el-icon v-else class="upload-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">空间背景图，建议尺寸：1920x1080像素</div>
        </el-form-item>
        
        <el-form-item label="昵称">
          <el-input v-model="settingsForm.nickname" maxlength="8" show-word-limit placeholder="请输入昵称" />
        </el-form-item>
        
        <el-form-item label="个人简介">
          <el-input v-model="settingsForm.bio" type="textarea" :rows="4" maxlength="50" show-word-limit placeholder="介绍一下自己吧" />
        </el-form-item>
        
        <el-form-item label="空间公告">
          <el-input v-model="settingsForm.announcement" type="textarea" :rows="3" maxlength="200" show-word-limit placeholder="发布空间公告" />
        </el-form-item>
        
        <el-form-item label="主题颜色">
          <el-color-picker v-model="settingsForm.themeColor" />
        </el-form-item>
        
        <el-form-item label="布局样式">
          <el-radio-group v-model="settingsForm.layout">
            <el-radio label="single">单栏布局</el-radio>
            <el-radio label="double">双栏布局</el-radio>
            <el-radio label="card">卡片式布局</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveSettings">保存设置</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
    <BackToTop />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import BackToTop from '@/components/BackToTop.vue'

const router = useRouter()
const userStore = useUserStore()

const settingsForm = ref({
  avatar: '',
  bgImage: '',
  nickname: '',
  bio: '',
  announcement: '',
  themeColor: '#f43f5e',
  layout: 'card'
})

const beforeAvatarUpload = async (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }

  try {
    // 上传头像到服务器
    const { uploadAvatar } = await import('@/api/user')
    const response = await uploadAvatar(file)
    
    if (response.data && response.data.url) {
      settingsForm.value.avatar = response.data.url
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败，请重试')
  }
  
  return false
}

const beforeBgUpload = async (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }

  try {
    // 上传背景图片到服务器
    const { uploadAvatar } = await import('@/api/user')
    const response = await uploadAvatar(file)
    
    if (response.data && response.data.url) {
      settingsForm.value.bgImage = response.data.url
      ElMessage.success('背景图片上传成功')
    }
  } catch (error) {
    console.error('背景图片上传失败:', error)
    ElMessage.error('背景图片上传失败，请重试')
  }
  
  return false
}

const saveSettings = async () => {
  // 验证必填项
  if (!settingsForm.value.nickname.trim()) {
    ElMessage.warning('请输入昵称')
    return
  }

  try {
    // 调用后端API更新用户信息
    const { updateUserProfile } = await import('@/api/user')
    await updateUserProfile({
      nickname: settingsForm.value.nickname,
      avatar: settingsForm.value.avatar,
      bio: settingsForm.value.bio
    })

    // 更新用户信息到 store
    userStore.updateUserInfo({
      nickname: settingsForm.value.nickname,
      bio: settingsForm.value.bio,
      avatar: settingsForm.value.avatar,
      bgImage: settingsForm.value.bgImage,
      announcement: settingsForm.value.announcement,
      themeColor: settingsForm.value.themeColor,
      layout: settingsForm.value.layout
    })

    ElMessage.success('保存成功')
    
    // 返回个人空间页面
    setTimeout(() => {
      router.push(`/space/${userStore.userInfo.id}`)
    }, 500)
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败，请重试')
  }
}

onMounted(() => {
  // 加载当前用户信息
  if (userStore.userInfo) {
    settingsForm.value.avatar = userStore.userInfo.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=user${userStore.userInfo.id}`
    settingsForm.value.bgImage = userStore.userInfo.bgImage || ''
    settingsForm.value.nickname = userStore.userInfo.nickname || ''
    settingsForm.value.bio = userStore.userInfo.bio || ''
    settingsForm.value.announcement = userStore.userInfo.announcement || ''
    settingsForm.value.themeColor = userStore.userInfo.themeColor || '#f43f5e'
    settingsForm.value.layout = userStore.userInfo.layout || 'card'
  } else {
    ElMessage.error('请先登录')
    router.push('/login')
  }
})
</script>


<style scoped>
.space-settings-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
  padding: 40px 20px;
}

.settings-container {
  max-width: 700px;
  margin: 0 auto;
  background: #fff;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(244, 63, 94, 0.08);
  border: 1px solid #ffe4e6;
}

.settings-container h2 {
  margin-bottom: 36px;
  font-size: 24px;
  color: #2d3436;
  font-weight: 600;
}

.settings-form {
  max-width: 100%;
}

.avatar-uploader {
  width: 120px;
  height: 120px;
  border: 2px dashed #fecdd3;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s;
}

.avatar-uploader:hover {
  border-color: #f43f5e;
  background: #fff1f2;
}

.avatar-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-uploader {
  width: 100%;
  max-width: 480px;
  height: 200px;
  border: 2px dashed #fecdd3;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s;
}

.cover-uploader:hover {
  border-color: #f43f5e;
  background: #fff1f2;
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-icon {
  font-size: 48px;
  color: #fb7185;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
}

.settings-form :deep(.el-button--primary) {
  background: #f43f5e;
  border-color: #f43f5e;
}

.settings-form :deep(.el-button--primary:hover) {
  background: #e11d48;
  border-color: #e11d48;
}
</style>
