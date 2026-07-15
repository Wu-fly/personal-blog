<template>
  <div class="messages-page">
    <div class="messages-container">
      <!-- 会话列表 -->
      <div class="conversation-list">
        <div class="list-header">
          <h3>私信 ({{ conversations.length }})</h3>
        </div>
        <div class="conversations">
          <div v-if="conversations.length === 0" style="padding: 20px; text-align: center; color: #999;">
            暂无会话
          </div>
          <div v-for="conv in conversations" :key="conv.id" :class="['conversation-item', { active: currentConversation?.id === conv.id }]" @click="selectConversation(conv)">
            <el-avatar :size="48" :src="conv.avatar" />
            <div class="conv-info">
              <div class="conv-header">
                <span class="name">{{ conv.nickname }}</span>
                <span class="time">{{ conv.lastTime }}</span>
              </div>
              <div class="last-message">{{ conv.lastMessage }}</div>
            </div>
            <el-badge v-if="conv.unreadCount" :value="conv.unreadCount" class="unread-badge" />
          </div>
        </div>
      </div>

      <!-- 聊天区域 -->
      <div class="chat-area">
        <template v-if="currentConversation">
          <div class="chat-header">
            <span class="chat-title">{{ currentConversation.nickname }}</span>
          </div>
          <div class="chat-messages" ref="messagesContainer">
            <div v-for="msg in messages" :key="msg.id" :class="['message-item', { mine: msg.isMine }]">
              <el-avatar :size="36" :src="msg.avatar" />
              <div class="message-content">
                <div class="message-text">{{ msg.content }}</div>
                <div class="message-time">{{ msg.createTime }}</div>
              </div>
            </div>
          </div>
          <div class="chat-input">
            <el-input v-model="messageContent" type="textarea" :rows="3" placeholder="输入消息..." @keyup.enter.ctrl="sendMessage" />
            <el-button type="primary" @click="sendMessage" :disabled="!messageContent.trim()">发送</el-button>
          </div>
        </template>
        <div v-else class="no-conversation">
          <el-empty description="选择一个会话开始聊天" />
        </div>
      </div>
    </div>
    <BackToTop />
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useRoute, useRouter } from 'vue-router'
import BackToTop from '@/components/BackToTop.vue'
import { getInbox, getConversation, sendMessage as sendMessageAPI, markAsRead, getOutbox } from '@/api/message'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const conversations = ref([])
const currentConversation = ref(null)
const messages = ref([])
const messageContent = ref('')
const messagesContainer = ref(null)
const loading = ref(false)

// 加载会话列表
const loadConversations = async () => {
  try {
    loading.value = true
    console.log('尝试加载会话列表...')
    
    // 同时加载收件箱和发件箱
    const [inboxResponse, outboxResponse] = await Promise.all([
      getInbox({ page: 0, size: 50 }),
      getOutbox({ page: 0, size: 50 })
    ])
    
    console.log('收件箱响应:', inboxResponse)
    console.log('发件箱响应:', outboxResponse)
    
    // 用于存储会话的Map，key是对话对象的ID
    const conversationMap = new Map()
    
    // 处理收件箱消息
    const processMessages = (messages, isSent = false) => {
      if (!messages || !Array.isArray(messages)) {
        console.log(`⚠ processMessages: 消息无效或不是数组, isSent=${isSent}`, messages)
        return
      }
      
      console.log(`处理${isSent ? '发件箱' : '收件箱'}消息，数量:`, messages.length)
      
      messages.forEach((msg, index) => {
        try {
          // 确定对话对象的ID（对方的ID）
          const otherUserId = isSent ? msg.receiverId : msg.senderId
          const otherNickname = isSent ? msg.receiverNickname : msg.senderNickname
          const otherAvatar = isSent ? msg.receiverAvatar : msg.senderAvatar
          
          console.log(`  消息${index + 1}: 对方ID=${otherUserId}, 昵称=${otherNickname}, 内容="${msg.content}"`)
          
          // 如果这个对话还不存在，或者当前消息更新，则更新会话信息
          if (!conversationMap.has(otherUserId) || 
              new Date(msg.createdAt) > new Date(conversationMap.get(otherUserId).lastTime)) {
            const convInfo = {
              id: otherUserId,
              nickname: otherNickname || `用户${otherUserId}`,
              avatar: otherAvatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=user${otherUserId}`,
              lastMessage: msg.content || '暂无消息',
              lastTime: msg.createdAt,
              unreadCount: (!isSent && !msg.isRead) ? 1 : 0
            }
            conversationMap.set(otherUserId, convInfo)
            console.log(`  ✓ 已添加/更新会话:`, convInfo)
          } else {
            console.log(`  - 跳过（已有更新的消息）`)
          }
        } catch (error) {
          console.error(`  ✗ 处理消息${index + 1}时出错:`, error)
        }
      })
    }
    
    // 处理收件箱
    try {
      if (inboxResponse && inboxResponse.messages) {
        console.log('处理收件箱消息（对象格式）')
        processMessages(inboxResponse.messages, false)
      } else if (inboxResponse && Array.isArray(inboxResponse)) {
        console.log('处理收件箱消息（数组格式）')
        processMessages(inboxResponse, false)
      } else {
        console.log('收件箱响应格式不正确:', inboxResponse)
      }
    } catch (error) {
      console.error('处理收件箱时出错:', error)
    }
    
    // 处理发件箱
    try {
      if (outboxResponse && outboxResponse.messages) {
        console.log('处理发件箱消息（对象格式）')
        processMessages(outboxResponse.messages, true)
      } else if (outboxResponse && Array.isArray(outboxResponse)) {
        console.log('处理发件箱消息（数组格式）')
        processMessages(outboxResponse, true)
      } else {
        console.log('发件箱响应格式不正确:', outboxResponse)
      }
    } catch (error) {
      console.error('处理发件箱时出错:', error)
    }
    
    console.log('会话Map大小:', conversationMap.size)
    console.log('会话Map内容:', Array.from(conversationMap.entries()))
    
    // 将Map转换为数组，并按最后消息时间排序
    let newConversations = []
    try {
      newConversations = Array.from(conversationMap.values())
        .sort((a, b) => new Date(b.lastTime) - new Date(a.lastTime))
        .map(conv => ({
          ...conv,
          lastTime: formatTime(conv.lastTime)
        }))
      console.log('✓ 会话转换成功')
    } catch (error) {
      console.error('✗ 会话转换失败:', error)
      return
    }
    
    console.log('新会话数量:', newConversations.length)
    
    // 只有当有新会话时才更新列表
    try {
      if (newConversations.length > 0) {
        // 直接替换会话列表（保留从URL参数创建的新会话）
        const urlCreatedConvs = conversations.value.filter(c => 
          c.lastMessage === '开始新对话' && !newConversations.find(nc => nc.id === c.id)
        )
        console.log('URL创建的会话:', urlCreatedConvs)
        conversations.value = [...newConversations, ...urlCreatedConvs]
        console.log('✓ 会话列表已更新，共', conversations.value.length, '个会话')
        console.log('✓ 最终会话列表:', conversations.value)
      } else {
        console.log('⚠ 没有新会话，保持现有会话列表，当前会话数:', conversations.value.length)
      }
    } catch (error) {
      console.error('✗ 更新会话列表失败:', error)
    }
  } catch (error) {
    console.error('✗ 加载会话列表失败:', error.message)
    console.log('这可能是新用户或API未实现，继续使用本地会话')
  } finally {
    loading.value = false
  }
}

// 创建新会话（当从个人空间点击发送私信时）
const createNewConversation = async (userId) => {
  console.log('创建新会话，用户ID:', userId)
  
  // 检查是否已存在该会话
  const existingConv = conversations.value.find(c => String(c.id) === String(userId))
  if (existingConv) {
    currentConversation.value = existingConv
    // 尝试加载历史消息
    try {
      await loadMessages(userId)
    } catch (error) {
      console.log('无法加载历史消息，可能是新对话')
      messages.value = []
    }
    return
  }
  
  // 创建一个新会话对象（先使用默认值）
  const newConv = {
    id: Number(userId),
    nickname: `用户${userId}`,
    avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=user${userId}`,
    lastMessage: '开始新对话',
    lastTime: '刚刚',
    unreadCount: 0
  }
  
  // 尝试从后端获取用户信息
  try {
    const { getUserSpace } = await import('@/api/user')
    const userInfo = await getUserSpace(userId)
    if (userInfo) {
      newConv.nickname = userInfo.nickname || `用户${userId}`
      newConv.avatar = userInfo.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=user${userId}`
      console.log('✓ 已从后端获取用户信息:', newConv.nickname)
    }
  } catch (error) {
    console.log('无法获取用户信息，使用默认值:', error.message)
  }
  
  conversations.value.unshift(newConv)
  currentConversation.value = newConv
  messages.value = []
  
  console.log('新会话已创建:', newConv)
}

// 选择会话
const selectConversation = async (conv) => {
  console.log('=== 选择会话 ===')
  console.log('会话对象:', conv)
  console.log('会话ID:', conv.id)
  
  currentConversation.value = conv
  messages.value = []
  
  console.log('✓ 已设置currentConversation和清空messages')
  
  // 标记为已读
  conv.unreadCount = 0
  
  // 尝试加载历史消息（如果API可用）
  try {
    console.log('准备调用loadMessages，用户ID:', conv.id)
    await loadMessages(conv.id)
    console.log('✓ loadMessages调用完成')
  } catch (error) {
    console.error('✗ loadMessages调用失败:', error)
    console.log('无法加载历史消息，可能是新对话')
    messages.value = []
  }
  
  console.log('=== 选择会话完成，当前消息数量:', messages.value.length, '===')
}

// 加载消息列表
const loadMessages = async (userId) => {
  try {
    console.log('=== 开始加载消息，用户ID:', userId, '===')
    const response = await getConversation(userId, { page: 0, size: 100 })
    console.log('消息API响应:', response)
    console.log('响应类型:', typeof response, '是否为数组:', Array.isArray(response))
    
    if (response && Array.isArray(response)) {
      console.log('✓ 响应是数组，消息数量:', response.length)
      
      const mappedMessages = response.map(msg => ({
        id: msg.id,
        content: msg.content,
        isMine: msg.senderId === userStore.userInfo?.id,
        nickname: msg.senderNickname,
        avatar: msg.senderId === userStore.userInfo?.id
          ? (userStore.userInfo?.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=user${userStore.userInfo?.id}`)
          : (msg.senderAvatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=user${msg.senderId}`),
        createTime: formatTime(msg.createdAt)
      }))
      
      console.log('映射后的消息:', mappedMessages)
      
      messages.value = mappedMessages.reverse() // 反转顺序，最新的在下面
      
      console.log('✓ 消息已设置，messages.value.length =', messages.value.length)
      
      // 更新会话中的用户昵称
      if (currentConversation.value && messages.value.length > 0) {
        const otherMsg = messages.value.find(m => !m.isMine)
        if (otherMsg && otherMsg.nickname) {
          currentConversation.value.nickname = otherMsg.nickname
          currentConversation.value.avatar = otherMsg.avatar
          console.log('✓ 已更新会话昵称:', otherMsg.nickname)
        }
      }
      
      await nextTick()
      scrollToBottom()
      console.log('=== 消息加载完成 ===')
    } else {
      console.log('⚠ 响应不是数组或为空')
    }
  } catch (error) {
    console.error('✗ 加载历史消息失败:', error)
    console.log('错误详情:', error.message, error.stack)
    // 不抛出错误，让用户可以开始新对话
  }
}

// 发送消息
const sendMessage = async () => {
  if (!messageContent.value.trim()) return
  if (!currentConversation.value) {
    ElMessage.warning('请先选择一个会话')
    return
  }
  
  const content = messageContent.value.trim()
  const tempId = Date.now()
  
  // 先在本地显示消息
  const newMsg = {
    id: tempId,
    content: content,
    isMine: true,
    avatar: userStore.userInfo?.avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=user${userStore.userInfo?.id}`,
    createTime: '刚刚'
  }
  messages.value.push(newMsg)
  
  // 更新会话最后消息
  currentConversation.value.lastMessage = content
  currentConversation.value.lastTime = '刚刚'
  
  // 清空输入框
  messageContent.value = ''
  await nextTick()
  scrollToBottom()
  
  // 尝试发送到后端
  try {
    console.log('发送消息到用户:', currentConversation.value.id)
    const response = await sendMessageAPI({
      receiverId: currentConversation.value.id,
      content: content
    })
    
    console.log('发送响应:', response)
    
    // 更新消息ID
    if (response && response.id) {
      newMsg.id = response.id
    }
    
    // 更新会话昵称（如果需要）
    if (response && response.receiverNickname && currentConversation.value.nickname.startsWith('用户')) {
      currentConversation.value.nickname = response.receiverNickname
      currentConversation.value.avatar = response.receiverAvatar || currentConversation.value.avatar
    }
    
    ElMessage.success('发送成功')
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.warning('消息已在本地显示，但发送到服务器失败')
  }
  
  // 将当前会话移到列表顶部
  const index = conversations.value.findIndex(c => c.id === currentConversation.value.id)
  if (index > 0) {
    const conv = conversations.value.splice(index, 1)[0]
    conversations.value.unshift(conv)
  }
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 172800000) return '昨天'
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString()
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

onMounted(async () => {
  console.log('=== Messages组件已挂载 ===')
  console.log('当前路由参数:', route.query)
  console.log('用户信息:', userStore.userInfo)
  console.log('初始会话列表长度:', conversations.value.length)
  
  // 先加载会话列表
  console.log('开始加载会话列表...')
  await loadConversations()
  console.log('=== 加载完成，最终会话列表长度:', conversations.value.length, '===')
  
  // 如果URL中有receiverId，选择对应的会话（而不是创建新会话）
  const receiverId = route.query.receiverId
  if (receiverId) {
    console.log('⚠ 检测到receiverId参数:', receiverId)
    
    // 查找是否已有该会话
    const existingConv = conversations.value.find(c => String(c.id) === String(receiverId))
    if (existingConv) {
      console.log('✓ 找到现有会话，直接选择')
      await selectConversation(existingConv)
    } else {
      console.log('⚠ 未找到现有会话，创建新对话')
      await createNewConversation(receiverId)
    }
    
    // 移除URL参数，避免刷新时重复创建
    router.replace({ query: {} })
    console.log('✓ 已移除URL参数')
  }
})
</script>


<style scoped>
.messages-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
}

.messages-container {
  display: flex;
  height: calc(100vh - 160px);
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(255, 107, 74, 0.08);
  border: 1px solid #fff5f0;
}

.conversation-list {
  width: 320px;
  border-right: 1px solid #fff5f0;
  display: flex;
  flex-direction: column;
  background: #fefcfb;
}

.list-header {
  padding: 24px;
  border-bottom: 1px solid #fff5f0;
}

.list-header h3 {
  margin: 0;
  color: #2d3436;
  font-size: 18px;
}

.conversations {
  flex: 1;
  overflow-y: auto;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
  border-left: 3px solid transparent;
}

.conversation-item:hover {
  background: #fff8f5;
}

.conversation-item.active {
  background: #fff8f5;
  border-left-color: #ff6b4a;
}

.conv-info {
  flex: 1;
  overflow: hidden;
}

.conv-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.name {
  font-weight: 600;
  color: #2d3436;
}

.time {
  color: #b2bec3;
  font-size: 12px;
}

.last-message {
  color: #636e72;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  padding: 24px;
  border-bottom: 1px solid #fff5f0;
  background: #fff;
}

.chat-title {
  font-weight: 600;
  font-size: 16px;
  color: #2d3436;
}

.chat-messages {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: #fefcfb;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.message-item.mine {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 60%;
}

.message-text {
  background: #fff;
  padding: 14px 18px;
  border-radius: 16px;
  line-height: 1.6;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  border: 1px solid #fff5f0;
}

.message-item.mine .message-text {
  background: linear-gradient(135deg, #ff6b4a, #ff9f7f);
  color: #fff;
  border: none;
  box-shadow: 0 4px 15px rgba(255, 107, 74, 0.3);
}

.message-time {
  font-size: 12px;
  color: #b2bec3;
  margin-top: 6px;
}

.chat-input {
  padding: 20px 24px;
  border-top: 1px solid #fff5f0;
  display: flex;
  gap: 16px;
  align-items: flex-end;
  background: #fff;
}

.chat-input .el-textarea {
  flex: 1;
}

.no-conversation {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fefcfb;
}
</style>
