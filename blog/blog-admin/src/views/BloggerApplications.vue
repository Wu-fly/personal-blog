<template>
  <div class="blogger-applications">
    <h1>博主申请审核</h1>
    
    <el-card>
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待审核" name="PENDING" />
        <el-tab-pane label="已通过" name="APPROVED" />
        <el-tab-pane label="已拒绝" name="REJECTED" />
      </el-tabs>

      <el-table :data="applications" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userName" label="申请人" width="120" />
        <el-table-column prop="userPhone" label="手机号" width="130" />
        <el-table-column prop="nickname" label="博主昵称" width="120" />
        <el-table-column prop="field" label="擅长领域" width="150">
          <template #default="{ row }">
            <span v-if="row.field">
              {{ row.field }}
              <el-tag v-if="isNewField(row.field)" type="success" size="small" style="margin-left: 5px">新</el-tag>
            </span>
            <span v-else style="color: #999">未填写</span>
          </template>
        </el-table-column>
        <el-table-column prop="bio" label="个人简介" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="申请时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PENDING'" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.status === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <template v-if="row.status === 'PENDING'">
              <el-button size="small" type="success" @click="approveApplication(row)">通过</el-button>
              <el-button size="small" type="danger" @click="rejectApplication(row)">拒绝</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadApplications"
        @current-change="loadApplications"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 申请详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="申请详情"
      width="600px"
    >
      <div v-if="currentApplication" class="application-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请ID">{{ currentApplication.id }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ currentApplication.userName }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentApplication.userPhone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentApplication.userEmail }}</el-descriptions-item>
          <el-descriptions-item label="博主昵称" :span="2">
            {{ currentApplication.nickname }}
          </el-descriptions-item>
          <el-descriptions-item label="擅长领域" :span="2">
            <span v-if="currentApplication.field">
              {{ currentApplication.field }}
              <el-tag v-if="isNewField(currentApplication.field)" type="success" size="small" style="margin-left: 5px">新</el-tag>
            </span>
            <span v-else style="color: #999">未填写</span>
          </el-descriptions-item>
          <el-descriptions-item label="个人简介" :span="2">
            {{ currentApplication.bio || '暂无' }}
          </el-descriptions-item>
          <el-descriptions-item label="申请时间" :span="2">
            {{ currentApplication.createdAt }}
          </el-descriptions-item>
          <el-descriptions-item label="审核状态" :span="2">
            <el-tag v-if="currentApplication.status === 'PENDING'" type="warning">待审核</el-tag>
            <el-tag v-else-if="currentApplication.status === 'APPROVED'" type="success">已通过</el-tag>
            <el-tag v-else type="danger">已拒绝</el-tag>
          </el-descriptions-item>
          <el-descriptions-item
            v-if="currentApplication.reviewComment"
            label="审核意见"
            :span="2"
          >
            {{ currentApplication.reviewComment }}
          </el-descriptions-item>
          <el-descriptions-item
            v-if="currentApplication.reviewedAt"
            label="审核时间"
            :span="2"
          >
            {{ currentApplication.reviewedAt }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <template v-if="currentApplication && currentApplication.status === 'PENDING'">
          <el-button type="success" @click="approveApplication(currentApplication)">通过</el-button>
          <el-button type="danger" @click="rejectApplication(currentApplication)">拒绝</el-button>
        </template>
      </template>
    </el-dialog>

    <!-- 拒绝原因对话框 -->
    <el-dialog
      v-model="rejectVisible"
      title="拒绝申请"
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
        <el-button type="danger" @click="confirmReject">确认拒绝</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getBloggerApplications, reviewBloggerApplication } from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const applications = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const activeTab = ref('PENDING')

const detailVisible = ref(false)
const currentApplication = ref(null)

const rejectVisible = ref(false)
const rejectForm = ref({
  comment: ''
})
const rejectingApplication = ref(null)

// 预定义的领域列表
const predefinedFields = [
  '技术',
  '生活',
  '旅游',
  '美食',
  '摄影',
  '音乐',
  '电影',
  '读书',
  '运动',
  '游戏'
]

// 判断是否为新领域
function isNewField(field) {
  if (!field) return false
  // 如果领域不在预定义列表中，则认为是新领域
  return !predefinedFields.includes(field)
}

// 加载申请列表
async function loadApplications() {
  try {
    loading.value = true
    const res = await getBloggerApplications({
      page: currentPage.value - 1,  // 后端页码从0开始
      size: pageSize.value,
      status: activeTab.value
    })
    if (res.code === 200 || res.success) {
      // 适配Spring Data Page对象
      const pageData = res.data
      applications.value = pageData.content || pageData.list || []
      total.value = pageData.totalElements || pageData.total || 0
    }
  } catch (error) {
    console.error('加载申请列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 切换标签页
function handleTabChange() {
  currentPage.value = 1
  loadApplications()
}

// 查看详情
function viewDetail(application) {
  currentApplication.value = application
  detailVisible.value = true
}

// 通过申请
async function approveApplication(application) {
  try {
    await ElMessageBox.confirm(`确认通过 ${application.nickname} 的博主申请吗？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })

    const res = await reviewBloggerApplication(application.id, {
      approved: true,
      reviewComment: ''
    })

    if (res.code === 200) {
      ElMessage.success('审核通过')
      detailVisible.value = false
      loadApplications()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审核失败:', error)
    }
  }
}

// 拒绝申请
function rejectApplication(application) {
  rejectingApplication.value = application
  rejectForm.value.comment = ''
  rejectVisible.value = true
  detailVisible.value = false
}

// 确认拒绝
async function confirmReject() {
  if (!rejectForm.value.comment.trim()) {
    ElMessage.warning('请输入拒绝原因')
    return
  }

  try {
    const res = await reviewBloggerApplication(rejectingApplication.value.id, {
      approved: false,
      reviewComment: rejectForm.value.comment
    })

    if (res.code === 200) {
      ElMessage.success('已拒绝该申请')
      rejectVisible.value = false
      loadApplications()
    }
  } catch (error) {
    console.error('拒绝失败:', error)
  }
}

onMounted(() => {
  loadApplications()
})
</script>

<style scoped>
.blogger-applications h1 {
  margin-bottom: 20px;
}
</style>
