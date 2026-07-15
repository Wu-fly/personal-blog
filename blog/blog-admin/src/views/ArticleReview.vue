<template>
  <div class="article-review">
    <h1>文章审核</h1>
    
    <el-card>
      <el-table :data="articles" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="authorName" label="作者" width="120" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column label="付费" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isPaid" type="warning">¥{{ row.price }}</el-tag>
            <el-tag v-else type="info">免费</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="提交时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="previewArticle(row)">预览</el-button>
            <el-button size="small" type="success" @click="approveArticle(row)">通过</el-button>
            <el-button size="small" type="danger" @click="rejectArticle(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadArticles"
        @current-change="loadArticles"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 文章预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="文章预览"
      width="70%"
      :close-on-click-modal="false"
    >
      <div v-if="currentArticle" class="article-preview">
        <h2>{{ currentArticle.title }}</h2>
        <div class="article-meta">
          <span>作者：{{ currentArticle.authorName }}</span>
          <span>分类：{{ currentArticle.categoryName }}</span>
          <span>提交时间：{{ currentArticle.createdAt }}</span>
          <span v-if="currentArticle.isPaid" class="paid-tag">付费文章 ¥{{ currentArticle.price }}</span>
        </div>
        <el-divider />
        <!-- 封面图片 -->
        <div v-if="currentArticle.coverImage" class="cover-image">
          <img :src="currentArticle.coverImage" alt="封面图片" />
        </div>
        <!-- 文章内容 -->
        <div class="article-content" v-html="processedContent"></div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">关闭</el-button>
        <el-button type="success" @click="approveArticle(currentArticle)">通过</el-button>
        <el-button type="danger" @click="rejectArticle(currentArticle)">拒绝</el-button>
      </template>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectVisible"
      title="拒绝文章"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="rejectForm" label-width="100px">
        <el-form-item label="拒绝原因">
          <el-input
            v-model="rejectForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入拒绝原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject" :loading="reviewing">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getPendingArticles, reviewArticle } from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const articles = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const previewVisible = ref(false)
const currentArticle = ref(null)

const rejectVisible = ref(false)
const rejectForm = ref({
  comment: ''
})
const rejectingArticle = ref(null)
const reviewing = ref(false) // 添加审核中状态,防止重复请求

// 将 Markdown 转换为 HTML
const convertMarkdownToHtml = (markdown) => {
  if (!markdown) return ''
  
  let html = markdown
  
  // 转义 HTML 特殊字符（但保留已有的 HTML 标签）
  const hasHtmlTags = /<[^>]+>/.test(html)
  if (!hasHtmlTags) {
    html = html
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
  }
  
  // 图片 - 必须在链接之前处理
  html = html.replace(/!\[(.*?)\]\((.*?)\)/g, '<img src="$2" alt="$1" />')
  
  // 链接
  html = html.replace(/\[(.*?)\]\((.*?)\)/g, '<a href="$2" target="_blank">$1</a>')
  
  // 标题
  html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>')
  html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>')
  html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>')
  
  // 粗体
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  
  // 斜体
  html = html.replace(/\*(.*?)\*/g, '<em>$1</em>')
  
  // 代码块
  html = html.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
  
  // 行内代码
  html = html.replace(/`(.*?)`/g, '<code>$1</code>')
  
  // 引用
  html = html.replace(/^&gt; (.*$)/gim, '<blockquote>$1</blockquote>')
  html = html.replace(/^> (.*$)/gim, '<blockquote>$1</blockquote>')
  
  // 无序列表
  html = html.replace(/^\* (.*$)/gim, '<li>$1</li>')
  html = html.replace(/^- (.*$)/gim, '<li>$1</li>')
  
  // 包裹列表项
  html = html.replace(/(<li>.*<\/li>)/s, '<ul>$1</ul>')
  
  // 段落 - 将连续的非标签行包裹在 <p> 中
  const lines = html.split('\n')
  let inParagraph = false
  let result = []
  
  for (let line of lines) {
    const trimmedLine = line.trim()
    
    // 如果是空行
    if (!trimmedLine) {
      if (inParagraph) {
        result.push('</p>')
        inParagraph = false
      }
      continue
    }
    
    // 如果是 HTML 标签开头
    if (trimmedLine.match(/^<(h[1-6]|ul|ol|li|blockquote|pre|code|img|div)/)) {
      if (inParagraph) {
        result.push('</p>')
        inParagraph = false
      }
      result.push(line)
    } else {
      // 普通文本
      if (!inParagraph) {
        result.push('<p>')
        inParagraph = true
      }
      result.push(line.replace(/\n/g, '<br>'))
    }
  }
  
  if (inParagraph) {
    result.push('</p>')
  }
  
  return result.join('\n')
}

// 计算属性：处理后的文章内容
const processedContent = computed(() => {
  if (!currentArticle.value || !currentArticle.value.content) return ''
  
  // 检查内容是否已经是 HTML 格式
  const content = currentArticle.value.content
  if (content.includes('<img') || content.includes('<h1') || content.includes('<h2')) {
    // 已经是 HTML 格式，直接返回
    return content
  }
  
  // 是 Markdown 格式，需要转换
  return convertMarkdownToHtml(content)
})

// 加载待审核文章
async function loadArticles() {
  try {
    loading.value = true
    const res = await getPendingArticles({
      page: currentPage.value - 1, // 后端页码从0开始，前端从1开始
      size: pageSize.value
    })
    if (res.code === 200) {
      // 后端返回的是Spring Data Page对象
      articles.value = res.data.content || []
      total.value = res.data.totalElements || 0
      
      // 处理文章数据，添加作者和分类名称
      articles.value = articles.value.map(article => ({
        ...article,
        authorName: article.user?.nickname || '未知',
        categoryName: article.category?.name || '未分类'
      }))
    }
  } catch (error) {
    console.error('加载文章失败:', error)
    ElMessage.error('加载文章失败')
  } finally {
    loading.value = false
  }
}

// 预览文章
function previewArticle(article) {
  currentArticle.value = article
  previewVisible.value = true
}

// 通过文章
async function approveArticle(article) {
  if (reviewing.value) return // 防止重复请求
  
  try {
    await ElMessageBox.confirm('确认通过该文章吗？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })

    reviewing.value = true
    const res = await reviewArticle(article.id, {
      approved: true,
      reviewComment: ''
    })

    if (res.code === 200) {
      ElMessage.success('审核通过')
      previewVisible.value = false
      loadArticles()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
      ElMessage.error('审核失败')
    }
  } finally {
    reviewing.value = false
  }
}

// 拒绝文章
function rejectArticle(article) {
  rejectingArticle.value = article
  rejectForm.value.comment = ''
  rejectVisible.value = true
  previewVisible.value = false
}

// 确认拒绝
async function confirmReject() {
  if (!rejectForm.value.comment.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  if (reviewing.value) return // 防止重复请求

  try {
    reviewing.value = true
    const res = await reviewArticle(rejectingArticle.value.id, {
      approved: false,
      reviewComment: rejectForm.value.comment
    })

    if (res.code === 200) {
      ElMessage.success('已拒绝该文章')
      rejectVisible.value = false
      loadArticles()
    }
  } catch (error) {
    console.error('拒绝失败:', error)
    ElMessage.error('拒绝失败')
  } finally {
    reviewing.value = false
  }
}

onMounted(() => {
  loadArticles()
})
</script>

<style scoped>
.article-review h1 {
  margin-bottom: 20px;
}

.article-preview h2 {
  margin-bottom: 10px;
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

.article-meta {
  display: flex;
  gap: 20px;
  color: #909399;
  font-size: 14px;
  flex-wrap: wrap;
}

.article-meta .paid-tag {
  color: #f56c6c;
  font-weight: 600;
}

.cover-image {
  margin: 20px 0;
  text-align: center;
}

.cover-image img {
  max-width: 100%;
  max-height: 400px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  object-fit: cover;
}

.article-content {
  line-height: 1.8;
  font-size: 16px;
  color: #2c3e50;
  word-wrap: break-word;
}

/* 确保文章内容中的图片能正确显示并居中 */
.article-content :deep(img) {
  display: block;
  margin: 20px auto;
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3) {
  margin: 20px 0 10px;
  font-weight: 600;
  color: #1f2937;
}

.article-content :deep(h1) {
  font-size: 28px;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 8px;
}

.article-content :deep(h2) {
  font-size: 24px;
}

.article-content :deep(h3) {
  font-size: 20px;
}

.article-content :deep(p) {
  margin: 12px 0;
  text-align: justify;
}

.article-content :deep(ul),
.article-content :deep(ol) {
  margin: 12px 0;
  padding-left: 24px;
}

.article-content :deep(li) {
  margin: 8px 0;
}

.article-content :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding-left: 16px;
  margin: 16px 0;
  color: #606266;
  font-style: italic;
}

.article-content :deep(code) {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 14px;
  color: #e74c3c;
}

.article-content :deep(pre) {
  background: #282c34;
  color: #abb2bf;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 16px 0;
}

.article-content :deep(pre code) {
  background: none;
  padding: 0;
  color: #abb2bf;
}

.article-content :deep(a) {
  color: #409eff;
  text-decoration: none;
  border-bottom: 1px solid #409eff;
  transition: all 0.3s;
}

.article-content :deep(a:hover) {
  color: #66b1ff;
  border-bottom-color: #66b1ff;
}

.article-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 16px 0;
}

.article-content :deep(table th),
.article-content :deep(table td) {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
  text-align: left;
}

.article-content :deep(table th) {
  background: #f5f7fa;
  font-weight: 600;
}
</style>
