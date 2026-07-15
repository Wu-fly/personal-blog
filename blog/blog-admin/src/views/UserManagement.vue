<template>
  <div class="user-management">
    <h1>用户管理</h1>
    
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="搜索">
          <el-input
            v-model="searchForm.keyword"
            placeholder="手机号/邮箱/昵称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="全部" clearable>
            <el-option label="普通用户" value="USER" />
            <el-option label="博主" value="BLOGGER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="正常" value="ACTIVE" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="users" v-loading="loading" style="width: 100%">
        <el-table-column label="序号" width="80" type="index" :index="indexMethod" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" />
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.role === 'ADMIN'" type="danger">管理员</el-tag>
            <el-tag v-else-if="row.role === 'BLOGGER'" type="success">博主</el-tag>
            <el-tag v-else type="info">普通用户</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'ACTIVE'" type="success">正常</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewUserDetail(row)">详情</el-button>
            <el-button
              v-if="row.status === 'ACTIVE'"
              size="small"
              type="danger"
              @click="disableUser(row)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              size="small"
              type="success"
              @click="enableUser(row)"
            >
              启用
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadUsers"
        @current-change="loadUsers"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 用户详情对话框 -->
    <el-dialog
      v-model="detailVisible"
      title="用户详情"
      width="600px"
    >
      <div v-if="currentUser" class="user-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ currentUser.nickname }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ currentUser.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ currentUser.email }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag v-if="currentUser.role === 'ADMIN'" type="danger">管理员</el-tag>
            <el-tag v-else-if="currentUser.role === 'BLOGGER'" type="success">博主</el-tag>
            <el-tag v-else type="info">普通用户</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="currentUser.status === 'ACTIVE'" type="success">正常</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="注册时间" :span="2">
            {{ currentUser.createdAt }}
          </el-descriptions-item>
          <el-descriptions-item label="个人简介" :span="2">
            {{ currentUser.bio || '暂无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserList, updateUserStatus } from '../api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const users = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const searchForm = ref({
  keyword: '',
  role: '',
  status: ''
})

const detailVisible = ref(false)
const currentUser = ref(null)

// 计算序号（考虑分页）
function indexMethod(index) {
  return (currentPage.value - 1) * pageSize.value + index + 1
}

// 加载用户列表
async function loadUsers() {
  try {
    loading.value = true
    const res = await getUserList({
      page: currentPage.value - 1, // Spring Page is 0-indexed
      size: pageSize.value,
      keyword: searchForm.value.keyword,
      role: searchForm.value.role,
      status: searchForm.value.status
    })
    console.log('User list response:', res)
    if (res.code === 200 || res.success !== false) {
      // Spring Page object has 'content' and 'totalElements'
      const pageData = res.data || res
      users.value = pageData.content || pageData.list || []
      total.value = pageData.totalElements || pageData.total || 0
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  currentPage.value = 1
  loadUsers()
}

// 重置
function handleReset() {
  searchForm.value = {
    keyword: '',
    role: '',
    status: ''
  }
  currentPage.value = 1
  loadUsers()
}

// 查看用户详情
function viewUserDetail(user) {
  currentUser.value = user
  detailVisible.value = true
}

// 禁用用户
async function disableUser(user) {
  try {
    await ElMessageBox.confirm(`确认禁用用户 ${user.nickname} 吗？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const res = await updateUserStatus(user.id, {
      status: 'DISABLED'
    })

    if (res.code === 200) {
      ElMessage.success('已禁用该用户')
      loadUsers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('禁用用户失败:', error)
    }
  }
}

// 启用用户
async function enableUser(user) {
  try {
    await ElMessageBox.confirm(`确认启用用户 ${user.nickname} 吗？`, '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })

    const res = await updateUserStatus(user.id, {
      status: 'ACTIVE'
    })

    if (res.code === 200) {
      ElMessage.success('已启用该用户')
      loadUsers()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('启用用户失败:', error)
    }
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management h1 {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>
