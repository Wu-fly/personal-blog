<template>
  <div class="wallet-page">
    <div class="wallet-container">
      <!-- 余额卡片 -->
      <div class="balance-card">
        <div class="balance-info">
          <span class="label">账户余额</span>
          <span class="amount">¥{{ balance.toFixed(2) }}</span>
        </div>
        <div class="balance-actions">
          <el-button type="primary" @click="showRechargeDialog">充值</el-button>
          <el-button @click="showWithdrawDialog">提现</el-button>
        </div>
      </div>

      <!-- 收益统计 -->
      <div class="income-stats">
        <div class="stat-item">
          <span class="value">¥{{ totalIncome.toFixed(2) }}</span>
          <span class="label">累计收益</span>
        </div>
        <div class="stat-item">
          <span class="value">¥{{ articleIncome.toFixed(2) }}</span>
          <span class="label">文章收益</span>
        </div>
        <div class="stat-item">
          <span class="value">¥{{ rewardIncome.toFixed(2) }}</span>
          <span class="label">打赏收益</span>
        </div>
      </div>

      <!-- 交易记录 -->
      <div class="transaction-section">
        <div class="section-header">
          <h3>交易记录</h3>
          <el-radio-group v-model="transactionType" size="small">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button value="income">收入</el-radio-button>
            <el-radio-button value="expense">支出</el-radio-button>
          </el-radio-group>
        </div>
        <el-table :data="transactions" style="width: 100%">
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="{ row }">
              <span :class="row.amount > 0 ? 'income' : 'expense'">
                {{ row.amount > 0 ? '+' : '' }}{{ row.amount.toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="100">
            <template #default="{ row }">
              <el-tag size="small">{{ getTypeText(row.type) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="180" />
        </el-table>
        <div class="pagination-wrapper">
          <el-pagination v-model:current-page="currentPage" :page-size="10" :total="total" layout="prev, pager, next" />
        </div>
      </div>
    </div>

    <!-- 充值弹窗 -->
    <el-dialog v-model="rechargeDialogVisible" title="充值" width="400px">
      <div class="recharge-options">
        <div v-for="amount in rechargeAmounts" :key="amount" :class="['amount-item', { active: rechargeAmount === amount }]" @click="rechargeAmount = amount">
          ¥{{ amount }}
        </div>
      </div>
      <el-input v-model.number="customRechargeAmount" placeholder="自定义金额" type="number" style="margin-top: 15px" />
      <template #footer>
        <el-button @click="rechargeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRecharge">确认充值</el-button>
      </template>
    </el-dialog>

    <!-- 提现弹窗 -->
    <el-dialog v-model="withdrawDialogVisible" title="提现" width="400px">
      <el-form :model="withdrawForm" label-width="80px">
        <el-form-item label="提现金额">
          <el-input v-model.number="withdrawForm.amount" type="number" placeholder="请输入提现金额" />
        </el-form-item>
        <el-form-item label="提现方式">
          <el-radio-group v-model="withdrawForm.method">
            <el-radio value="alipay">支付宝</el-radio>
            <el-radio value="wechat">微信</el-radio>
            <el-radio value="bank">银行卡</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="账号">
          <el-input v-model="withdrawForm.account" placeholder="请输入收款账号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="withdrawDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmWithdraw">确认提现</el-button>
      </template>
    </el-dialog>

    <BackToTop />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useWalletStore } from '@/stores/wallet'
import { getWalletBalance, getTransactions, recharge as rechargeApi, withdraw as withdrawApi } from '@/api/wallet'
import BackToTop from '@/components/BackToTop.vue'

const walletStore = useWalletStore()

const balance = ref(0)
const totalIncome = ref(0)
const totalWithdraw = ref(0)

// 计算文章收益和打赏收益
const articleIncome = computed(() => {
  return allTransactions.value
    .filter(t => t.type === 'INCOME' && t.amount > 0)
    .reduce((sum, t) => sum + t.amount, 0)
})

const rewardIncome = computed(() => {
  return allTransactions.value
    .filter(t => t.type === 'REWARD' && t.amount > 0)
    .reduce((sum, t) => sum + t.amount, 0)
})

const transactionType = ref('all')
const transactions = ref([])
const allTransactions = ref([])
const currentPage = ref(1)
const total = ref(0)

// 充值
const rechargeDialogVisible = ref(false)
const rechargeAmounts = [10, 50, 100, 200, 500]
const rechargeAmount = ref(50)
const customRechargeAmount = ref(null)

// 提现
const withdrawDialogVisible = ref(false)
const withdrawForm = reactive({
  amount: null,
  method: 'alipay',
  account: ''
})

const getTypeText = (type) => {
  const texts = { 
    RECHARGE: '充值', 
    WITHDRAW: '提现', 
    PURCHASE: '购买', 
    REWARD: '打赏', 
    INCOME: '收益' 
  }
  return texts[type] || type
}

const showRechargeDialog = () => {
  rechargeDialogVisible.value = true
}

const showWithdrawDialog = () => {
  withdrawDialogVisible.value = true
}

const confirmRecharge = async () => {
  const amount = customRechargeAmount.value || rechargeAmount.value
  
  try {
    await rechargeApi({ amount })
    ElMessage.success(`充值 ¥${amount} 成功`)
    rechargeDialogVisible.value = false
    // 重新加载数据
    await loadWalletData()
  } catch (error) {
    console.error('充值失败:', error)
    ElMessage.error(error.message || '充值失败')
  }
}

const confirmWithdraw = async () => {
  if (!withdrawForm.amount || withdrawForm.amount <= 0) {
    ElMessage.warning('请输入正确的提现金额')
    return
  }
  
  try {
    await withdrawApi({ 
      amount: withdrawForm.amount,
      method: withdrawForm.method,
      account: withdrawForm.account
    })
    ElMessage.success('提现申请已提交')
    withdrawDialogVisible.value = false
    // 重新加载数据
    await loadWalletData()
  } catch (error) {
    console.error('提现失败:', error)
    ElMessage.error(error.message || '提现失败')
  }
}

const filterTransactions = () => {
  if (transactionType.value === 'all') {
    transactions.value = allTransactions.value
  } else if (transactionType.value === 'income') {
    transactions.value = allTransactions.value.filter(t => t.amount > 0)
  } else {
    transactions.value = allTransactions.value.filter(t => t.amount < 0)
  }
  total.value = transactions.value.length
}

// 加载钱包数据
const loadWalletData = async () => {
  try {
    // 获取余额信息
    const balanceData = await getWalletBalance()
    balance.value = balanceData.balance || 0
    totalIncome.value = balanceData.totalIncome || 0
    totalWithdraw.value = balanceData.totalWithdraw || 0
    
    // 获取交易记录
    const transactionsData = await getTransactions({ page: 0, size: 100 })
    // 响应拦截器已经解包，transactionsData就是Page对象
    if (transactionsData && transactionsData.content) {
      allTransactions.value = transactionsData.content.map(t => ({
        id: t.id,
        description: t.description,
        amount: parseFloat(t.amount),
        type: t.type,
        createTime: t.createdAt?.replace('T', ' ').substring(0, 19) || ''
      }))
      filterTransactions()
    }
  } catch (error) {
    console.error('加载钱包数据失败:', error)
    ElMessage.error('加载钱包数据失败')
  }
}

// 监听筛选变化
watch(transactionType, filterTransactions)

onMounted(() => {
  loadWalletData()
})
</script>


<style scoped>
.wallet-page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 30px 20px;
}

.balance-card {
  background: linear-gradient(135deg, #ff6b4a 0%, #ffb347 100%);
  border-radius: 24px;
  padding: 40px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  box-shadow: 0 10px 40px rgba(255, 107, 74, 0.3);
}

.balance-info .label {
  font-size: 14px;
  opacity: 0.9;
}

.balance-info .amount {
  font-size: 42px;
  font-weight: 700;
  display: block;
  margin-top: 10px;
  text-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.balance-actions {
  display: flex;
  gap: 15px;
}

.balance-actions .el-button {
  background: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.3);
  color: #fff;
}

.balance-actions .el-button:hover {
  background: rgba(255,255,255,0.3);
}

.income-stats {
  display: flex;
  gap: 24px;
  margin-bottom: 30px;
}

.stat-item {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(255, 107, 74, 0.08);
  border: 1px solid #fff5f0;
  transition: transform 0.3s;
}

.stat-item:hover {
  transform: translateY(-4px);
}

.stat-item .value {
  font-size: 28px;
  font-weight: 700;
  color: #ff6b4a;
  display: block;
}

.stat-item .label {
  color: #636e72;
  font-size: 14px;
  margin-top: 8px;
}

.transaction-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(255, 107, 74, 0.08);
  border: 1px solid #fff5f0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  color: #2d3436;
}

.income { color: #00b894; font-weight: 600; }
.expense { color: #ff6b4a; font-weight: 600; }

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.recharge-options {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.amount-item {
  width: 90px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #ffe4d9;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

.amount-item:hover {
  border-color: #ff6b4a;
  color: #ff6b4a;
}

.amount-item.active {
  border-color: #ff6b4a;
  color: #fff;
  background: linear-gradient(135deg, #ff6b4a, #ffb347);
}
</style>
