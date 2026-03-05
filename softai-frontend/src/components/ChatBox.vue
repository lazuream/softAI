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
      <!-- 会话ID面板 -->
      <div class="session-id-panel">
        <p class="session-id-label">当前会话ID：</p>
        <p class="session-id-value">{{ currentSessionId }}</p>
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

        <!-- 消息列表 - 支持可点击链接 -->
        <div 
          v-for="(msg, idx) in currentChat.messages" 
          :key="idx" 
          class="msg" 
          :class="msg.role"
        >
          <div class="avatar">
            <el-icon :icon="msg.role === 'user' ? 'User' : 'Robot'" size="16" />
          </div>
          <div class="bubble" v-html="formatContentWithLinks(msg.content)"></div>
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
import { generateSessionId } from '../utils/index.js'
import { sendChatStream } from '../api/chat.js'

// 当前会话ID
const currentSessionId = ref(generateSessionId())

// URL转可点击链接的工具函数
const formatContentWithLinks = (content) => {
  if (!content) return ''
  const urlRegex = /(https?:\/\/[^\s]+)/g
  return content.replace(urlRegex, (url) => {
    return `<a href="${url}" target="_blank" style="color: #2563eb; text-decoration: underline; word-break: break-all;">${url}</a>`
  })
}

// 初始化对话列表
const chatList = ref([
  {
    title: '',
    messages: [{ role: 'ai', content: '你好！我是 AI 智能助手，请问有什么可以帮助你的？' }],
    sessionId: currentSessionId.value
  }
])

const activeIdx = ref(0)
const inputText = ref('')
const loading = ref(false)
const msgBoxRef = ref(null)

// 当前激活的对话
const currentChat = computed(() => {
  return chatList.value[activeIdx.value] || { messages: [], sessionId: '' }
})

// 获取对话标题
const getChatTitle = (chatItem) => {
  if (!Array.isArray(chatItem.messages) || chatItem.messages.length === 0) {
    return '新对话'
  }
  const firstUserMsg = chatItem.messages.find(msg => msg.role === 'user')
  if (firstUserMsg) {
    return firstUserMsg.content.slice(0, 20) + (firstUserMsg.content.length > 20 ? '...' : '')
  }
  const firstMsg = chatItem.messages[0]
  return firstMsg ? firstMsg.content.slice(0, 20) + (firstMsg.content.length > 20 ? '...' : '') : '新对话'
}

// 核心修复：先解析新闻，再解析普通对话
const sendMsg = async () => {
  const txt = inputText.value.trim()
  if (!txt || loading.value) return
  if (!Array.isArray(currentChat.value.messages)) {
    currentChat.value.messages = []
  }
  // 添加用户消息
  currentChat.value.messages.push({ role: 'user', content: txt })
  inputText.value = ''
  loading.value = true
  nextTick(scrollToBottom)

  try {
    const url = `http://localhost:8084/agent/sendMessage?chatId=${currentSessionId.value}&message=${encodeURIComponent(txt)}`
    const res = await fetch(url, {
      method: 'GET',
      headers: { 'Accept': 'application/json' }
    })
    if (!res.ok) throw new Error(`接口返回异常：${res.status}`)
    const responseData = await res.json()
    if (responseData.status !== 'success') {
      throw new Error(responseData.info || '请求失败')
    }

    const dataStr = responseData.data
    // ========== 第一步：优先解析新闻数据（多个JSON对象拼接） ==========
    const jsonObjects = []
    const regex = /\{[\s\S]*?\}(?=\s*\{|\s*$)/g
    let match
    while ((match = regex.exec(dataStr)) !== null) {
      try {
        const obj = JSON.parse(match[0])
        // 验证是否为有效新闻对象（包含num/title/abstract/link字段）
        if (obj.num && obj.title && obj.abstract && obj.link) {
          jsonObjects.push(obj)
        }
      } catch (e) {
        console.warn('解析新闻JSON对象失败:', match[0])
      }
    }

    // 如果解析出有效新闻列表，套用新闻模版
    if (jsonObjects.length > 0) {
      let formattedContent = '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n';
      formattedContent += '                             📰 AI新闻周报                              \n';
      formattedContent += '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n';
      jsonObjects.forEach((item, index) => {
        formattedContent += `【${item.num}】${item.title}\n`;
        formattedContent += `📝 ${item.abstract}\n`;
        formattedContent += `🔗 阅读全文：${item.link}\n`;
        if (index < jsonObjects.length - 1) {
          formattedContent += '──────────────────────────────────────────────────\n';
        }
      });
      formattedContent += '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n';
      formattedContent += '                            新闻来源：AI资讯                            \n';
      formattedContent += '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━';
      currentChat.value.messages.push({ role: 'ai', content: formattedContent });
    } else {
      // ========== 第二步：解析普通CHAT对话（嵌套JSON） ==========
      try {
        const coreData = JSON.parse(dataStr)
        // 普通CHAT对话：提取纯文本
        if (coreData.intentType === 'CHAT') {
          currentChat.value.messages.push({
            role: 'ai',
            content: coreData.data || '暂无回复内容'
          })
        } else {
          // 其他类型消息
          currentChat.value.messages.push({
            role: 'ai',
            content: `收到其他类型消息: ${coreData.intentType}`
          })
        }
      } catch (e) {
        // ========== 第三步：兜底：直接返回纯文本 ==========
        currentChat.value.messages.push({ role: 'ai', content: dataStr })
      }
    }

    nextTick(scrollToBottom)
  } catch (err) {
    // 接口失败兜底
    currentChat.value.messages.push({
      role: 'ai',
      content: `抱歉，请求失败【会话ID：${currentSessionId.value}】：` + (err.message || '服务器无响应')
    })
  } finally {
    loading.value = false
    nextTick(scrollToBottom)
  }
}

// 新建对话
const addNewChat = () => {
  const newSessionId = generateSessionId()
  chatList.value.push({
    title: '',
    messages: [{ role: 'ai', content: '你好！我是 AI 智能助手，请问有什么可以帮助你的？' }],
    sessionId: newSessionId
  })
  activeIdx.value = chatList.value.length - 1
  currentSessionId.value = newSessionId
  nextTick(scrollToBottom)
}

// 切换对话
const switchChat = (idx) => {
  activeIdx.value = idx
  currentSessionId.value = chatList.value[idx].sessionId || generateSessionId()
  nextTick(scrollToBottom)
}

// 清空当前对话
const clearCurrentChat = () => {
  currentChat.value.messages = []
}

// 滚动到底部
const scrollToBottom = () => {
  const el = msgBoxRef.value
  if (el) el.scrollTop = el.scrollHeight
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

/* 会话ID面板 */
.session-id-panel {
  padding: 16px 20px;
  border-top: 1px solid #e5e7eb;
  background-color: #f9fafb;
}

.session-id-label {
  font-size: 12px;
  color: #6b7280;
  margin: 0 0 4px 0;
}

.session-id-value {
  font-size: 14px;
  color: #2563eb;
  font-weight: 500;
  margin: 0;
  letter-spacing: 0.5px;
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
  white-space: pre-wrap;
  word-break: break-word;
}

.msg.user .bubble {
  background: #2563eb;
  color: white;
}

/* 修复用户侧链接样式 */
.msg.user .bubble a {
  color: #bfdbfe !important;
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