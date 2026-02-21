<template>
  <div class="ai-chat-layout">
    <!-- 左侧：对话历史 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h3>对话历史</h3>
        <el-button type="primary" size="small" @click="addNewChat">+ 新对话</el-button>
      </div>
      <div class="history-list">
        <div
          v-for="(item, idx) in chatList"
          :key="idx"
          class="history-item"
          :class="{ active: idx === activeIdx }"
          @click="switchChat(idx)"
        >
          {{ getChatTitle(item) }}
        </div>
      </div>
    </div>

    <!-- 右侧：聊天窗口 -->
    <div class="chat-window">
      <div class="chat-header">
        <h2>{{ getChatTitle(currentChat) }}</h2>
        <el-button type="text" @click="clearCurrentChat">清空对话</el-button>
      </div>

      <div class="message-box" ref="msgBoxRef">
        <!-- 空状态 -->
        <div v-if="!currentChat.messages || currentChat.messages.length === 0" class="empty-state">
          <el-icon size="60" icon="Message" />
          <p>开始与 AI 助手对话吧 💬</p>
        </div>

        <!-- 消息列表 -->
        <div 
          v-for="(msg, idx) in currentChat.messages" 
          :key="idx" 
          class="msg" 
          :class="msg.role"
        >
          <div class="avatar">
            <el-icon :icon="msg.role === 'user' ? 'User' : 'Robot'" size="16" />
          </div>
          <div class="bubble">{{ msg.content }}</div>
          <div class="msg-time">{{ formatTime(new Date()) }}</div>
        </div>

        <!-- 加载中 -->
        <div v-if="loading" class="msg ai">
          <div class="avatar"><el-icon icon="Robot" size="16" /></div>
          <div class="bubble typing">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div class="input-bar">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="3"
          placeholder="输入消息，按 Enter 发送"
          @keyup.enter="sendMsg"
          :disabled="loading"
        />
        <div class="input-actions">
          <el-button 
            type="text" 
            icon="Clear" 
            @click="inputText = ''"
            :disabled="!inputText || loading"
          >清空</el-button>
          <el-button 
            type="primary" 
            @click="sendMsg" 
            :loading="loading"
            :disabled="!inputText.trim() || loading"
          >发送</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'

// 初始化对话列表（确保messages一定是数组）
const chatList = ref([
  {
    title: '',
    messages: [{ role: 'ai', content: '你好！我是 AI 智能助手，请问有什么可以帮助你的？' }]
  }
])

const activeIdx = ref(0)
const inputText = ref('')
const loading = ref(false)
const msgBoxRef = ref(null)

// 当前激活的对话（加兜底，防止undefined）
const currentChat = computed(() => {
  return chatList.value[activeIdx.value] || { messages: [] }
})

// 修复核心：获取对话标题（增加数组判断）
const getChatTitle = (chatItem) => {
  // 先判断messages是否是数组，不是则返回默认标题
  if (!Array.isArray(chatItem.messages) || chatItem.messages.length === 0) {
    return '新对话'
  }
  // 找第一条用户消息
  const firstUserMsg = chatItem.messages.find(msg => msg.role === 'user')
  if (firstUserMsg) {
    return firstUserMsg.content.slice(0, 20) + (firstUserMsg.content.length > 20 ? '...' : '')
  }
  // 没有用户消息则用AI第一条消息
  const firstMsg = chatItem.messages[0]
  return firstMsg ? firstMsg.content.slice(0, 20) + (firstMsg.content.length > 20 ? '...' : '') : '新对话'
}

// 发送消息
const sendMsg = async () => {
  const txt = inputText.value.trim()
  if (!txt || loading.value) return

  // 确保messages是数组
  if (!Array.isArray(currentChat.value.messages)) {
    currentChat.value.messages = []
  }

  // 添加用户消息
  currentChat.value.messages.push({ role: 'user', content: txt })
  inputText.value = ''
  loading.value = true

  // 滚动到底部
  nextTick(scrollToBottom)

  // ========== 适配后端流式响应（Flux<String>）的核心代码 ==========
  try {
    // 1. 拼接接口地址 + 编码参数
    const url = `http://localhost:8080/ai/chat?prompt=${encodeURIComponent(txt)}`
    
    // 2. 发送GET请求，接收流式响应
    const res = await fetch(url, {
      method: 'GET',
      headers: {
        'Accept': 'text/event-stream, text/html', // 告诉后端接收流式数据
      }
    })

    if (!res.ok) throw new Error(`接口返回异常：${res.status}`)
    
    // 3. 检查是否有可读流
    if (!res.body) throw new Error('后端未返回流式数据')
    
    // 4. 创建AI回复占位（用于实时更新）
    const aiMsgIndex = currentChat.value.messages.length
    currentChat.value.messages.push({
      role: 'ai',
      content: '' // 初始为空，后续逐字拼接
    })

    // 5. 解析流式响应
    const reader = res.body.getReader()
    const decoder = new TextDecoder('utf-8') // 解码UTF-8数据
    let fullContent = '' // 存储完整的AI回复

    // 6. 循环读取流数据（逐段接收后端返回的内容）
    while (true) {
      const { done, value } = await reader.read()
      
      // 流结束则退出循环
      if (done) break
      
      // 解码并拼接内容
      const chunk = decoder.decode(value, { stream: true })
      fullContent += chunk
      
      // 实时更新AI回复内容
      currentChat.value.messages[aiMsgIndex].content = fullContent
      
      // 每次更新后滚动到底部
      nextTick(scrollToBottom)
    }

    // 7. 流读取完成，确保最后一段内容解码
    fullContent += decoder.decode()
    currentChat.value.messages[aiMsgIndex].content = fullContent

  } catch (err) {
    // 接口失败的兜底提示
    currentChat.value.messages.push({
      role: 'ai',
      content: '抱歉，请求失败：' + (err.message || '服务器无响应')
    })
  } finally {
    // 关闭加载状态
    loading.value = false
    nextTick(scrollToBottom)
  }
}

// 新建对话
const addNewChat = () => {
  chatList.value.push({
    title: '',
    messages: [{ role: 'ai', content: '你好！我是 AI 智能助手，请问有什么可以帮助你的？' }]
  })
  activeIdx.value = chatList.value.length - 1
  nextTick(scrollToBottom)
}

// 切换对话
const switchChat = (idx) => {
  activeIdx.value = idx
  nextTick(scrollToBottom)
}

// 清空当前对话
const clearCurrentChat = () => {
  currentChat.value.messages = []
}

// 滚动到底部
const scrollToBottom = () => {
  const el = msgBoxRef.value
  if (el) {
    el.scrollTop = el.scrollHeight
  }
}

// 时间格式化
const formatTime = (date) => {
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false
  })
}
</script>

<style scoped>
/* 整体布局 */
.ai-chat-layout {
  display: flex;
  width: 100%;
  height: 100vh;
  background: #f5f7fa;
  overflow: hidden;
}

/* 左侧侧边栏 */
.sidebar {
  width: 280px;
  background: #ffffff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 16px;
  color: #1f2937;
}

.history-list {
  flex: 1;
  padding: 8px 0;
  overflow-y: auto;
}

.history-item {
  padding: 12px 20px;
  cursor: pointer;
  font-size: 14px;
  color: #4b5563;
  border-left: 3px solid transparent;
  transition: all 0.2s ease;
}

.history-item:hover {
  background-color: #f3f4f6;
}

.history-item.active {
  background-color: #eff6ff;
  color: #2563eb;
  border-left-color: #2563eb;
}

/* 右侧聊天窗口 */
.chat-window {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header h2 {
  margin: 0;
  font-size: 18px;
  color: #1f2937;
}

/* 消息区域 */
.message-box {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background: #f9fafb;
}

/* 空状态 */
.empty-state {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #9ca3af;
  gap: 12px;
}

/* 消息项 */
.msg {
  display: flex;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.msg.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #4b5563;
}

.msg.user .avatar {
  background: #2563eb;
  color: white;
}

.bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 16px;
  background: white;
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
  font-size: 14px;
  line-height: 1.5;
  margin: 0 12px;
}

.msg.user .bubble {
  background: #2563eb;
  color: white;
}

.msg-time {
  font-size: 12px;
  color: #9ca3af;
  align-self: flex-end;
  margin-bottom: 4px;
}

/* 加载动画 */
.typing {
  display: flex;
  gap: 4px;
  align-items: center;
  padding: 12px 16px;
}

.typing span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #2563eb;
  animation: bounce 1.4s infinite ease-in-out;
}

.typing span:nth-child(1) { animation-delay: -0.32s; }
.typing span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

/* 输入区域 */
.input-bar {
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
}

.input-bar .el-input {
  margin-bottom: 12px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

/* 滚动条美化 */
.message-box::-webkit-scrollbar,
.history-list::-webkit-scrollbar {
  width: 6px;
}

.message-box::-webkit-scrollbar-track,
.history-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.message-box::-webkit-scrollbar-thumb,
.history-list::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.message-box::-webkit-scrollbar-thumb:hover,
.history-list::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>