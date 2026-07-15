import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useWalletStore = defineStore('wallet', () => {
  // 状态
  const balance = ref(0)
  const totalIncome = ref(0)
  const totalWithdraw = ref(0)
  const transactions = ref([])
  const rechargeRecords = ref([])
  const withdrawRecords = ref([])
  const rewardRecords = ref([])
  const revenueDetails = ref([])
  
  // 方法
  function setBalance(amount) {
    balance.value = amount
  }
  
  function setTotalIncome(amount) {
    totalIncome.value = amount
  }
  
  function setTotalWithdraw(amount) {
    totalWithdraw.value = amount
  }
  
  function setTransactions(list) {
    transactions.value = list
  }
  
  function setRechargeRecords(list) {
    rechargeRecords.value = list
  }
  
  function setWithdrawRecords(list) {
    withdrawRecords.value = list
  }
  
  function setRewardRecords(list) {
    rewardRecords.value = list
  }
  
  function setRevenueDetails(list) {
    revenueDetails.value = list
  }
  
  function updateBalance(amount) {
    balance.value += amount
  }
  
  function clearWallet() {
    balance.value = 0
    totalIncome.value = 0
    totalWithdraw.value = 0
    transactions.value = []
    rechargeRecords.value = []
    withdrawRecords.value = []
    rewardRecords.value = []
    revenueDetails.value = []
  }
  
  return {
    balance,
    totalIncome,
    totalWithdraw,
    transactions,
    rechargeRecords,
    withdrawRecords,
    rewardRecords,
    revenueDetails,
    setBalance,
    setTotalIncome,
    setTotalWithdraw,
    setTransactions,
    setRechargeRecords,
    setWithdrawRecords,
    setRewardRecords,
    setRevenueDetails,
    updateBalance,
    clearWallet
  }
})
