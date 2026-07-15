<template>
  <div class="article-editor-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">{{ isEdit ? '编辑文章' : '文章创建' }}</h1>
    </div>
    
    <div class="editor-container">
      <!-- 标题输入 -->
      <el-input v-model="articleForm.title" placeholder="请输入文章标题" class="title-input" maxlength="100" show-word-limit />

      <!-- 工具栏 -->
      <div class="editor-toolbar">
        <el-select v-model="articleForm.categoryId" placeholder="选择分类" style="width: 150px">
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
        </el-select>
        <el-select 
          v-model="articleForm.tags" 
          multiple 
          filterable 
          allow-create 
          default-first-option
          :reserve-keyword="false"
          placeholder="输入1-3个标签（按回车添加）" 
          style="width: 300px"
          @change="handleTagsChange"
        >
          <el-option v-for="tag in tags" :key="tag" :label="tag" :value="tag" />
        </el-select>
        <el-switch v-model="articleForm.isPaid" active-text="付费文章" />
        <el-input-number v-if="articleForm.isPaid" v-model="articleForm.price" :min="1" :precision="2" placeholder="价格" style="width: 120px" />
      </div>

      <!-- 内容编辑器 - 左右分栏 -->
      <div class="editor-wrapper">
        <div class="editor-panel">
          <div class="panel-header">编辑</div>
          <el-input 
            ref="contentEditor"
            v-model="articleForm.content" 
            type="textarea" 
            :rows="20" 
            placeholder="请输入文章内容（支持Markdown，可直接粘贴图片）"
            @paste="handlePaste"
            class="markdown-editor"
          />
        </div>
        <div class="preview-panel">
          <div class="panel-header">预览</div>
          <div class="markdown-preview" v-html="renderedContent"></div>
        </div>
      </div>

      <!-- 封面图片 -->
      <div class="cover-section">
        <span class="label">封面图片：</span>
        <el-upload class="cover-uploader" action="#" :show-file-list="false" :before-upload="beforeCoverUpload">
          <img v-if="articleForm.coverImage" :src="articleForm.coverImage" class="cover-preview" />
          <el-icon v-else class="upload-icon"><Plus /></el-icon>
        </el-upload>
      </div>

      <!-- 操作按钮 -->
      <div class="editor-actions">
        <el-button @click="saveDraft">保存草稿</el-button>
        <el-button type="primary" @click="submitArticle">
          {{ isEdit ? '更新文章' : '提交审核' }}
        </el-button>
        <el-button v-if="isEdit" type="danger" @click="deleteArticle">删除文章</el-button>
      </div>
    </div>
    <BackToTop />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import BackToTop from '@/components/BackToTop.vue'

const route = useRoute()
const router = useRouter()

const articleForm = reactive({
  title: '',
  content: '',
  categoryId: null,
  tags: [],
  isPaid: false,
  price: 0,
  coverImage: ''
})

const categories = ref([])

// 简单的 Markdown 渲染函数
const renderedContent = computed(() => {
  if (!articleForm.content) return '<p class="empty-hint">在左侧输入内容，这里会实时预览...</p>'
  
  let html = articleForm.content
  
  // 转义 HTML 特殊字符
  html = html
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
  
  // 标题
  html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>')
  html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>')
  html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>')
  
  // 粗体
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  
  // 斜体
  html = html.replace(/\*(.*?)\*/g, '<em>$1</em>')
  
  // 图片 - 重要：要在链接之前处理
  html = html.replace(/!\[(.*?)\]\((.*?)\)/g, '<img src="$2" alt="$1" style="max-width: 100%; height: auto; border-radius: 8px; margin: 10px 0;" />')
  
  // 链接
  html = html.replace(/\[(.*?)\]\((.*?)\)/g, '<a href="$2" target="_blank">$1</a>')
  
  // 代码块
  html = html.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
  
  // 行内代码
  html = html.replace(/`(.*?)`/g, '<code>$1</code>')
  
  // 引用
  html = html.replace(/^&gt; (.*$)/gim, '<blockquote>$1</blockquote>')
  
  // 无序列表
  html = html.replace(/^\* (.*$)/gim, '<li>$1</li>')
  html = html.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>')
  
  // 段落
  html = html.split('\n\n').map(para => {
    if (para.match(/^<[h|u|o|p|b]/)) return para
    return `<p>${para.replace(/\n/g, '<br>')}</p>`
  }).join('\n')
  
  return html
})

// 加载分类列表
const loadCategories = async () => {
  try {
    const { getCategories } = await import('@/api/article')
    const response = await getCategories()
    categories.value = response || []
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  }
}

// 标签改为字符串数组
const tags = ref([
  '胡雪岩',
  '徽商',
  '晚清',
  '商业',
  '历史',
  '金融',
  '人物',
  '经商之道'
])

// 加载自定义标签
const loadCustomTags = () => {
  try {
    const customTagsStr = localStorage.getItem('customTags')
    if (customTagsStr) {
      const customTags = JSON.parse(customTagsStr)
      // 合并自定义标签到标签列表
      customTags.forEach(tag => {
        if (!tags.value.includes(tag)) {
          tags.value.push(tag)
        }
      })
    }
  } catch (error) {
    console.error('加载自定义标签失败:', error)
  }
}

// 处理标签变化
const handleTagsChange = (newTags) => {
  // 限制最多3个标签
  if (newTags.length > 3) {
    ElMessage.warning('最多只能添加3个标签')
    articleForm.tags = newTags.slice(0, 3)
    return
  }
  
  // 保存新标签到localStorage
  try {
    const customTags = JSON.parse(localStorage.getItem('customTags') || '[]')
    let hasNewTag = false
    
    newTags.forEach(tag => {
      // 如果是新标签（不在预定义列表中）
      if (!tags.value.includes(tag) && !customTags.includes(tag)) {
        customTags.push(tag)
        tags.value.push(tag)
        hasNewTag = true
      }
    })
    
    if (hasNewTag) {
      localStorage.setItem('customTags', JSON.stringify(customTags))
    }
  } catch (error) {
    console.error('保存自定义标签失败:', error)
  }
}
const isEdit = ref(false)

const beforeCoverUpload = async (file) => {
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
    // 上传封面图片到服务器
    const { uploadImage } = await import('@/api/upload')
    const uploadData = await uploadImage(file)
    
    console.log('封面上传响应:', uploadData)
    
    // 响应拦截器已经解包，uploadData就是UploadResponse对象
    if (uploadData && uploadData.url) {
      articleForm.coverImage = uploadData.url
      console.log('封面URL已更新:', uploadData.url)
      ElMessage.success('封面上传成功')
    } else {
      console.error('上传响应中没有URL:', uploadData)
      ElMessage.error('封面上传失败：响应格式错误')
    }
  } catch (error) {
    console.error('封面上传失败:', error)
    ElMessage.error('封面上传失败：' + (error.message || '请重试'))
  }
  
  return false
}

// 添加 contentEditor 的 ref
const contentEditor = ref(null)

// 处理粘贴事件
const handlePaste = async (event) => {
  const items = event.clipboardData?.items
  if (!items) return

  // 检查是否包含图片
  let hasImage = false
  let imageFile = null
  
  for (let i = 0; i < items.length; i++) {
    const item = items[i]
    if (item.type.indexOf('image') !== -1) {
      hasImage = true
      imageFile = item.getAsFile()
      break
    }
  }

  // 如果没有图片,让默认粘贴行为继续
  if (!hasImage || !imageFile) return

  // 有图片时,阻止默认行为并手动处理
  event.preventDefault()

  // 获取粘贴的文本内容
  const pastedText = event.clipboardData?.getData('text/plain') || ''

  // 检查文件大小
  const isLt5M = imageFile.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    // 即使图片太大,也要粘贴文字
    if (pastedText) {
      const textarea = contentEditor.value?.$el?.querySelector('textarea')
      if (textarea) {
        const cursorPos = textarea.selectionStart
        const textBefore = articleForm.content.substring(0, cursorPos)
        const textAfter = articleForm.content.substring(cursorPos)
        articleForm.content = textBefore + pastedText + textAfter
        
        setTimeout(() => {
          const newPos = cursorPos + pastedText.length
          textarea.setSelectionRange(newPos, newPos)
          textarea.focus()
        }, 0)
      }
    }
    return
  }

  try {
    ElMessage.info('正在上传图片...')
    
    // 上传图片到服务器
    const { uploadImage } = await import('@/api/upload')
    const uploadData = await uploadImage(imageFile)
    
    if (uploadData && uploadData.url) {
      // 获取当前光标位置
      const textarea = contentEditor.value?.$el?.querySelector('textarea')
      if (textarea) {
        const cursorPos = textarea.selectionStart
        const textBefore = articleForm.content.substring(0, cursorPos)
        const textAfter = articleForm.content.substring(cursorPos)
        
        // 构建要插入的内容：文字 + 图片
        const imageMarkdown = `![图片](${uploadData.url})`
        let insertContent = ''
        
        if (pastedText) {
          // 如果有文字,先插入文字,再插入图片
          insertContent = pastedText + '\n' + imageMarkdown + '\n'
        } else {
          // 只有图片
          insertContent = '\n' + imageMarkdown + '\n'
        }
        
        articleForm.content = textBefore + insertContent + textAfter
        
        // 设置新的光标位置
        setTimeout(() => {
          const newPos = cursorPos + insertContent.length
          textarea.setSelectionRange(newPos, newPos)
          textarea.focus()
        }, 0)
        
        ElMessage.success('图片上传成功')
      }
    } else {
      ElMessage.error('图片上传失败：响应格式错误')
      // 图片上传失败,至少保留文字
      if (pastedText) {
        const textarea = contentEditor.value?.$el?.querySelector('textarea')
        if (textarea) {
          const cursorPos = textarea.selectionStart
          const textBefore = articleForm.content.substring(0, cursorPos)
          const textAfter = articleForm.content.substring(cursorPos)
          articleForm.content = textBefore + pastedText + textAfter
          
          setTimeout(() => {
            const newPos = cursorPos + pastedText.length
            textarea.setSelectionRange(newPos, newPos)
            textarea.focus()
          }, 0)
        }
      }
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error('图片上传失败：' + (error.message || '请重试'))
    // 图片上传失败,至少保留文字
    if (pastedText) {
      const textarea = contentEditor.value?.$el?.querySelector('textarea')
      if (textarea) {
        const cursorPos = textarea.selectionStart
        const textBefore = articleForm.content.substring(0, cursorPos)
        const textAfter = articleForm.content.substring(cursorPos)
        articleForm.content = textBefore + pastedText + textAfter
        
        setTimeout(() => {
          const newPos = cursorPos + pastedText.length
          textarea.setSelectionRange(newPos, newPos)
          textarea.focus()
        }, 0)
      }
    }
  }
}


const saveDraft = async () => {
  // TODO: 保存草稿
  ElMessage.success('草稿已保存')
}

const submitArticle = async () => {
  if (!articleForm.title.trim()) {
    ElMessage.warning('请输入文章标题')
    return
  }
  if (!articleForm.content.trim()) {
    ElMessage.warning('请输入文章内容')
    return
  }
  if (!articleForm.categoryId) {
    ElMessage.warning('请选择文章分类')
    return
  }
  
  try {
    const { createArticle, updateArticle } = await import('@/api/article')
    
    // 准备提交数据
    const submitData = {
      title: articleForm.title,
      content: articleForm.content,
      summary: articleForm.content.substring(0, 200), // 自动生成摘要
      categoryId: articleForm.categoryId,
      // 确保tagNames是字符串数组
      tagNames: articleForm.tags.map(tag => typeof tag === 'string' ? tag : tag.name),
      isPaid: articleForm.isPaid,
      price: articleForm.isPaid ? parseFloat(articleForm.price) : null, // 非付费文章price为null
      coverImage: articleForm.coverImage || ''
    }
    
    console.log('提交数据:', JSON.stringify(submitData, null, 2))
    
    if (isEdit.value) {
      // 更新文章
      await updateArticle(route.params.id, submitData)
      ElMessage.success('文章已更新')
    } else {
      // 创建新文章
      await createArticle(submitData)
      ElMessage.success('文章已提交审核')
    }
    
    setTimeout(() => {
      router.push('/user-center')
    }, 1000)
  } catch (error) {
    console.error('提交文章失败:', error)
    ElMessage.error(error.response?.data?.message || '提交失败，请重试')
  }
}

const deleteArticle = async () => {
  // 确认删除
  if (confirm('确定要删除这篇文章吗？')) {
    // TODO: 调用删除API
    ElMessage.success('文章已删除')
    setTimeout(() => {
      router.push('/user-center')
    }, 1000)
  }
}

onMounted(() => {
  // 加载分类和标签
  loadCategories()
  loadCustomTags()
  
  const id = route.params.id
  if (id) {
    isEdit.value = true
    // 从后端加载文章数据
    loadArticleData(id)
  }
})

const loadArticleData = async (id) => {
  try {
    const { getArticleDetail } = await import('@/api/article')
    const article = await getArticleDetail(id, true) // 传递forEdit=true
    
    console.log('加载的文章数据:', article)
    
    if (article) {
      // 映射后端数据到表单
      articleForm.title = article.title || ''
      articleForm.content = article.content || ''
      articleForm.categoryId = article.categoryId || null
      articleForm.tags = article.tags || []
      articleForm.isPaid = article.isPaid || false
      articleForm.price = article.price || 0
      articleForm.coverImage = article.coverImage || ''
      
      ElMessage.success('文章加载成功')
    }
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('文章不存在或加载失败')
    // 3秒后返回个人中心
    setTimeout(() => {
      router.push('/user-center')
    }, 3000)
  }
}
</script>


<style scoped>
.article-editor-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 20px;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
  background: linear-gradient(135deg, #f43f5e 0%, #ff6b4a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.editor-container {
  background: #fff;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(255, 107, 74, 0.08);
  border: 1px solid #fff5f0;
}

.title-input {
  margin-bottom: 24px;
}

.title-input :deep(.el-input__inner) {
  font-size: 28px;
  font-weight: 700;
  border: none;
  padding: 0;
  color: #2d3436;
}

.title-input :deep(.el-input__inner::placeholder) {
  color: #b2bec3;
}

.editor-toolbar {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #fff5f0;
}

.content-editor {
  margin-bottom: 24px;
}

.content-editor :deep(.el-textarea__inner) {
  font-size: 16px;
  line-height: 2;
  border-radius: 12px;
  border-color: #ffe4d9;
}

.content-editor :deep(.el-textarea__inner:focus) {
  border-color: #ff6b4a;
}

.cover-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 36px;
}

.cover-section .label {
  color: #636e72;
  font-weight: 500;
}

.cover-uploader {
  width: 220px;
  height: 130px;
  border: 2px dashed #ffe4d9;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s;
}

.cover-uploader:hover {
  border-color: #ff6b4a;
  background: #fff8f5;
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-icon {
  font-size: 36px;
  color: #ffb347;
}

.editor-actions {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
}

/* 编辑器和预览面板样式 */
.editor-wrapper {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.editor-panel,
.preview-panel {
  border: 1px solid #ffe4d9;
  border-radius: 12px;
  overflow: hidden;
}

.panel-header {
  background: linear-gradient(135deg, #fff5f0 0%, #ffe4d9 100%);
  padding: 12px 20px;
  font-weight: 600;
  color: #ff6b4a;
  border-bottom: 1px solid #ffe4d9;
}

.markdown-editor :deep(.el-textarea__inner) {
  font-size: 16px;
  line-height: 2;
  border: none;
  border-radius: 0;
  min-height: 500px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.markdown-preview {
  padding: 20px;
  min-height: 500px;
  max-height: 500px;
  overflow-y: auto;
  background: #fff;
  font-size: 16px;
  line-height: 1.8;
  color: #2d3436;
}

.markdown-preview .empty-hint {
  color: #b2bec3;
  font-style: italic;
  text-align: center;
  padding: 100px 20px;
}

.markdown-preview h1 {
  font-size: 32px;
  font-weight: 700;
  margin: 24px 0 16px;
  color: #1f2937;
  border-bottom: 2px solid #ffe4d9;
  padding-bottom: 8px;
}

.markdown-preview h2 {
  font-size: 28px;
  font-weight: 600;
  margin: 20px 0 12px;
  color: #374151;
}

.markdown-preview h3 {
  font-size: 24px;
  font-weight: 600;
  margin: 16px 0 10px;
  color: #4b5563;
}

.markdown-preview p {
  margin: 12px 0;
  text-align: justify;
}

.markdown-preview strong {
  font-weight: 700;
  color: #ff6b4a;
}

.markdown-preview em {
  font-style: italic;
  color: #6b7280;
}

.markdown-preview code {
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 14px;
  color: #ff6b4a;
}

.markdown-preview pre {
  background: #1f2937;
  color: #f3f4f6;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
}

.markdown-preview pre code {
  background: none;
  padding: 0;
  color: #f3f4f6;
}

.markdown-preview blockquote {
  border-left: 4px solid #ff6b4a;
  padding-left: 16px;
  margin: 16px 0;
  color: #6b7280;
  font-style: italic;
}

.markdown-preview ul,
.markdown-preview ol {
  margin: 12px 0;
  padding-left: 24px;
}

.markdown-preview li {
  margin: 8px 0;
}

.markdown-preview a {
  color: #ff6b4a;
  text-decoration: none;
  border-bottom: 1px solid #ffe4d9;
  transition: all 0.3s;
}

.markdown-preview a:hover {
  color: #f43f5e;
  border-bottom-color: #f43f5e;
}

.markdown-preview img {
  display: block;
  margin: 20px auto;
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
</style>
