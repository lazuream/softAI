import axios from 'axios'

// 创建 axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 60000, // AI 响应可能较慢，延长超时时间
  headers: {
    'Content-Type': 'application/json'
  }
})

// 聊天请求接口
export const sendChatMessage = (message) => {
  return request.post('/chat', {
    content: message
  })
}

// 新增：流式聊天请求（返回原生Promise，用于fetch兼容）
export const sendChatStream = (prompt, chatId) => {
  const url = `/chat?prompt=${encodeURIComponent(prompt)}&chatId=${chatId}`
  return fetch(url, {
    method: 'GET',
    headers: {
      'Accept': 'text/html, text/event-stream;charset=UTF-8'
    }
  })
}


// 可选：获取模型列表（如果后端有该接口）
export const getModelList = () => {
  return request.get('/models')
}