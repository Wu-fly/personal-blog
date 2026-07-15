<template>
  <div class="admin-wallet">
    <h1>平台钱包</h1>
    
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <div class="wallet-stat">
            <div class="stat-icon" style="background-color: #409EFF;">
              <el-icon size="32"><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">当前余额</div>
              <div class="stat-value">¥{{ (walletInfo.balance || 0).toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <div class="wallet-stat">
            <div class="stat-icon" style="background-color: #67C23A;">
              <el-icon size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总收益</div>
              <div class="stat-value">¥{{ (walletInfo.totalIncome || 0).toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <div class="wallet-stat">
            <div class="stat-icon" style="background-color: #E6A23C;">
              <el-icon size="32"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总提现</div>
              <div class="stat-value">¥{{ (walletInfo.totalWithdraw || 0).toFixed(2) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>收益明细</span>
          <el-button type="primary" @click="withdrawVisible = true">申请提现</el-button>
        </div>
      </template>

      <el-table :data="revenues" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'INCOME'" type="success">收入</el-tag>
            <el-tag v-else-if="row.type === 'WITHDRAW'" type="warning">提现</el-tag>
            <el-tag v-else-if="row.type === 'REWARD'" type="info">打赏</el-tag>
            <el-tag v-else-if="row.type === 'PURCHASE'" type="primary">购买</el-tag>
            <el-tag v-else>其他</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.amount >= 0 ? '#67C23A' : '#E6A23C' }">
              {{ row.amount >= 0 ? '+' : '' }}¥{{ Math.abs(row.amount).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="200" />
        <el-table-column label="关联文章" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.relatedArticleTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'SUCCESS'" type="success">成功</el-tag>
            <el-tag v-else-if="row.status === 'PENDING'" type="warning">处理中</el-tag>
            <el-tag v-else type="danger">失败</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadRevenues"
        @current-change="loadRevenues"
        style="margin-top: 20px; justify-content: center;"
      />
    </el-card>

    <!-- 提现对话框 -->
    <el-dialog
      v-model="withdrawVisible"
      title="申请提现"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="withdrawForm" :rules="withdrawRules" ref="withdrawFormRef" label-width="100px">
        <el-form-item label="当前余额">
          <span style="font-size: 18px; font-weight: bold; color: #409EFF;">
            ¥{{ (walletInfo.balance || 0).toFixed(2) }}
          </span>
        </el-form-item>
        <el-form-item label="提现金额" prop="amount">
          <el-input-number
            v-model="withdrawForm.amount"
            :min="0.01"
            :max="walletInfo.balance"
            :precision="2"
            :step="100"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="提现说明" prop="description">
          <el-input
            v-model="withdrawForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入提现说明（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="withdrawVisible = false">取消</el-button>
        <el-button type="primary" @click="handleWithdraw" :loading="withdrawing">确认提现</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Wallet, TrendCharts, Money } from '@element-plus/icons-vue'
import { getAdminWallet, getAdminRevenue, adminWithdraw } from '../api/admin'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const walletInfo = ref({})
const revenues = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const withdrawVisible = ref(false)
const withdrawing = ref(false)
const withdrawFormRef = ref(null)
const withdrawForm = reactive({
  amount: 0,
  description: ''
})

const withdrawRules = {
  amount: [
    { required: true, message: '请输入提现金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '提现金额必须大于0', trigger: 'blur' }
  ]
}

// 加载钱包信息
async function loadWalletInfo() {
  try {
    const res = await getAdminWallet()
    if ((res.success || res.code === 200) && res.data) {
      walletInfo.value = res.data
    }
  } catch (error) {
    console.error('加载钱包信息失败:', error)
    // 显示默认值
    walletInfo.value = {
      balance: 0,
      totalIncome: 0,
      totalWithdraw: 0
    }
  }
}

// 加载收益明细
async function loadRevenues() {
  try {
    loading.value = true
    const res = await getAdminRevenue({
      page: currentPage.value - 1, // 后端从0开始
      size: pageSize.value
    })
    if ((res.success || res.code === 200) && res.data) {
      revenues.value = res.data.content || res.data.list || []
      total.value = res.data.totalElements || res.data.total || 0
    }
  } catch (error) {
    console.error('加载收益明细失败:', error)
    revenues.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 提现
async function handleWithdraw() {
  try {
    await withdrawFormRef.value.validate()

    if (withdrawForm.amount > walletInfo.value.balance) {
      ElMessage.warning('提现金额不能大于当前余额')
      return
    }

    withdrawing.value = true
    const res = await adminWithdraw({
      amount: withdrawForm.amount,
      description: withdrawForm.description
    })

    if (res.success || res.code === 200) {
      ElMessage.success('提现申请已提交')
      withdrawVisible.value = false
      withdrawForm.amount = 0
      withdrawForm.description = ''
      loadWalletInfo()
      loadRevenues()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提现失败:', error)
    }
  } finally {
    withdrawing.value = false
  }
}

// 格式化日期
function formatDate(dateStr) {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadWalletInfo()
  loadRevenues()
})
</script>

<style scoped>
.admin-wallet h1 {
  margin-bottom: 20px;
}

.wallet-stat {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
