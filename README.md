# SoftAI 项目配置说明
# 项目概述
该项目是基于 Spring AI 构建的 AI 交互应用，通过配置文件 application.yml 管理核心参数，其中 API Key 是调用阿里百炼 AI 接口的关键凭证，需按要求替换为有效密钥才能正常使用。
## 核心配置 - API Key 填写步骤
1. 定位配置位置
打开配置文件：softai-web/src/main/resources/application.yml，找到以下核心配置段：
```yaml
spring:
  ai:
    openai:
      api-key: sk-xxxxxxxxxxxx
      base-url: https://dashscope.aliyuncs.com/compatible-mode/
```
2. 替换 API Key
前往阿里百炼控制台获取你的专属 API Key（AppKey）；
将配置中 api-key 后的 sk-xxxxxxxxxxxx 替换为实际获取的 API Key，示例：
```yaml
api-key: sk-1234567890abcdefghijklmnopqrstuvwxyz
```
3. 保存文件，确保 YAML 格式正确（冒号后保留空格，缩进与原有配置一致）。
其他关键配置说明
端口配置
```yaml
server:
  port: 8084
项目默认启动端口为 8084，若端口被占用，可修改此数值（如 8080、8888 等）。
如需启用本地 Ollama 服务，取消注释并配置 base-url（Ollama 服务地址）和 model（本地模型名称），同时将 ollama.enabled 改为 true。
AI 模型与交互配置
Ollama 本地模型配置（可选）
    ollama:
      base-url: http://localhost:11434
      chat:
        model: deepseek-r1:7b
```

```yaml
chat:
  completions-path: v1/chat/completions
  options:
    model: qwen-plus  #qwen3-max 使用max 调用mcp 报错 toolName cannot be null
    enable_thinking: false
```
model：默认使用 qwen-plus 模型，避免使用 qwen3-max（调用 MCP 会报 toolName cannot be null 错误）；
enable_thinking：是否启用思考模式，默认关闭。
## MCP 客户端配置（目前使用mcp调用工作流有误）

```yaml
mcp:
  client:
    enabled: true
    name: my-mcp-client
    version: 1.0.0
    request-timeout: 30s
    type: SYNC  # or ASYNC for reactive applications
    streamable-http:
      connections:
        mcp-server:
          url: http://localhost:8085
```
enabled：是否启用 MCP 客户端，默认开启；
type：通信类型，同步场景用 SYNC，异步响应式场景用 ASYNC；
url：MCP 服务端地址，默认本地 8085 端口，需与实际部署的 MCP 服务地址一致；
request-timeout：请求超时时间，默认 30 秒，可按需调整。
日志配置
```yaml
logging:
  level:
    org.springframework.ai.mcp: debug
    org.springframework.ai.tool: debug
    org.springframework.ai.chat.client.advisor: debug
    com.lazuream.ai: debug
```
项目默认开启 AI 相关模块的调试日志，便于排查问题；若需降低日志级别，可将 debug 改为 info/warn/error。
## 注意事项
密钥安全：API Key 是敏感信息，禁止提交到公共代码仓库，确保 .gitignore 已忽略 application.yml 文件；
格式校验：修改 YAML 文件时严格遵守语法规范，缩进、空格错误会导致配置加载失败；
权限验证：确保阿里百炼 API Key 未过期，且已开通 qwen-plus 等模型的调用权限；
依赖检查：启动前确认 JDK 17+、Maven 3.8+ 环境已配置，避免因环境问题启动失败。
启动项目
运行
进入项目目录
```
cd softai-web
```
编译打包
```
mvn clean package
```
启动 jar 包
```
java -jar target/softai-0.0.1-SNAPSHOT.jar
```
启动成功后，可访问 http://localhost:8084 调用项目接口（具体接口需参考项目业务文档）。
# 常见问题
## Q1：启动后提示 API Key 无效 / 鉴权失败？
## A1：
核对 API Key 是否填写正确（无多余空格、大小写一致）；
检查阿里百炼账号余额及模型调用限额；
确认 base-url 为阿里百炼 OpenAI 兼容接口固定地址：https://dashscope.aliyuncs.com/compatible-mode/。
## Q2：调用 MCP 时报 toolName cannot be null？
## A2：
将 AI 模型从 qwen3-max 切换为 qwen-plus/qwen-turbo；
检查 MCP 客户端配置是否完整，无缺失字段。
## Q3：MCP 客户端连接失败？
## A3：
确认 MCP 服务端已启动且地址（url）配置正确；
检查网络是否互通，防火墙是否放行 8085 端口；
适当延长 request-timeout 时间，避免因超时导致连接失败。这段也转成类似的code格式
