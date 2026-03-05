package com.lazuream.ai.component;



import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.app.FlowStreamMode;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.lazuream.ai.component.LoggerAdvisor;
import com.lazuream.ai.entity.enums.PromptTypeEnum;
import com.lazuream.ai.utils.StringTools;
import io.reactivex.Flowable;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChatComponent {

    @Resource
    private ChatClient chatClient;

    // 百炼工作流应用 ID
    private static final String APP_ID = "deec5a17c5e64f87ad3c573fc8afe777";


    /**
     * 聊天消息数据结构
     */
    public record ChatMessageData(String chatId, String message, Long timestamp) {
    }

    /**`
     * 发送消息并获取 AI 回复（支持 MCP 工具调用）
     */
    public String send(String chatId, String userMessage) {
        try {
            log.info("开始处理用户消息：chatId={}, message={}", chatId, userMessage);

            // 分析用户意图
            UserIntent userIntent = analyzeUserIntent(chatId, userMessage);
            PromptTypeEnum promptTypeEnum = PromptTypeEnum.getByCode(userIntent.intentType);
            promptTypeEnum = promptTypeEnum == null ? PromptTypeEnum.CHAT : promptTypeEnum;

            log.info("意图识别完成：intentType={}, data={}", userIntent.intentType, promptTypeEnum);

            // 根据意图类型调用不同的处理方法
            switch (promptTypeEnum) {
                case AI_NEWS_SEARCH:
                    log.info("识别到 AI 新闻查询意图，开始调用工作流...");
                    return getAiNews();

                case CHAT:
                default:
                    log.info("进入普通聊天模式");
                    return chat(promptTypeEnum, chatId, userMessage);
            }

        } catch (Exception e) {
            log.error("调用大模型失败", e);
            return "抱歉，处理您的请求时出现错误：" + e.getMessage();
        }
    }

    /**
     * 查询 AI 热门新闻（直接从 ChatService 迁移过来的逻辑）
     */
    public String getAiNews() {
        try {
            log.info("----------------------开始获取 AI 新闻----------------------");

            String apiKey = "sk-c4358ded11ee4e389090c30528227ecb";

            // 使用流式调用工作流
            StringBuilder fullContent = new StringBuilder();
            streamCall(apiKey, APP_ID, "请汇总近一周 AI 领域的 10 条热门新闻，要求包含标题、摘要和来源链接，按重要性排序。", fullContent);

            log.info("AI 新闻获取成功，结果长度：{}", fullContent.length());
            return fullContent.toString();

        } catch (Exception e) {
            log.error("获取 AI 新闻失败", e);
            return "获取 AI 新闻失败：" + e.getMessage();
        }
    }

    /**
     * 流式调用工作流
     */
    private void streamCall(String apiKey, String appId, String prompt, StringBuilder resultCollector)
            throws ApiException, NoApiKeyException, InputRequiredException {

        log.info("开始流式调用百炼工作流，appId: {}", appId);

        // 构建参数（流式）
        ApplicationParam param = ApplicationParam.builder()
                .apiKey(apiKey)
                .appId(appId)
                .flowStreamMode(FlowStreamMode.MESSAGE_FORMAT) // 启用流式输出
                .prompt(prompt)
                .build();

        Application application = new Application();

        // 执行流式调用
        Flowable<ApplicationResult> resultFlow = application.streamCall(param);

        // 处理流式结果
        resultFlow.blockingForEach(data -> {
            if (data.getOutput() != null) {
                // 检查是否是流式消息
                if (data.getOutput().getWorkflowMessage() != null
                        && data.getOutput().getWorkflowMessage().getMessage() != null) {
                    String content = data.getOutput().getWorkflowMessage().getMessage().getContent();
                    if (content != null) {
                        log.debug("流式内容：{}", content);
                        resultCollector.append(content);
                    }
                }

                // 检查是否结束
                if (data.getOutput().getFinishReason() != null
                        && data.getOutput().getFinishReason().equals("stop")) {
                    String text = data.getOutput().getText();
                    if (text != null) {
                        log.info("\n任务完成，最终文本：{}", text);
                    }
                }
            }
        });
    }

    /**
     * 大模型组织语言回答（普通聊天）
     */
    private String chat(PromptTypeEnum promptTypeEnum, String chatId, String userMessage) {
        List<String> chatMessage = new ArrayList<>();

        try {
            String prompt = getPrompt(promptTypeEnum, chatId, userMessage);

            log.info("开始调用大模型，提示词类型：{}", promptTypeEnum);

            // 使用 CountDownLatch 等待异步处理完成
            java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            final boolean[] success = {false};

            getChatClientRequestSpec()
                    .messages(getHistoryMessage(prompt))
                    .stream()
                    .chatResponse()
                    .doOnNext(response -> {
                        String responseMessage = response.getResults().get(0).getOutput().getText();
                        if (!StringTools.isEmpty(responseMessage)) {
                            log.debug("流式输出内容：{}", responseMessage);
                            chatMessage.add(responseMessage);
                        }
                    })
                    .doOnComplete(() -> {
                        String finalResponse = chatMessage.stream().collect(Collectors.joining(""));
                        log.info("大模型回复完成，总长度：{}", finalResponse.length());
                        success[0] = true;
                        latch.countDown();
                    })
                    .doOnError(error -> {
                        log.error("大模型调用失败", error);
                        latch.countDown();
                    })
                    .subscribe();

            // 等待处理完成（最多等待 30 秒）
            if (!latch.await(30, java.util.concurrent.TimeUnit.SECONDS)) {
                log.warn("AI 响应超时");
                return "抱歉，AI 助手响应超时，请稍后再试。";
            }

            if (success[0]) {
                return chatMessage.stream().collect(Collectors.joining(""));
            } else {
                return "抱歉，AI 助手处理您的请求时出现错误，请稍后再试。";
            }

        } catch (Exception e) {
            log.error("调用大模型失败", e);
            return "抱歉，AI 助手暂时无法响应，请稍后再试。";
        }
    }

    /**
     * 获取历史消息
     */
    private List<Message> getHistoryMessage(String currentMessage) {
        List<Message> messages = new ArrayList<>();
        messages.add(new UserMessage(currentMessage));
        return messages;
    }

    /**
     * 获取提示词
     */
    private String getPrompt(PromptTypeEnum promptType, String chatId, String message) {
        String prompt = promptType.getPrompt();
        if (PromptTypeEnum.CHAT == promptType) {
            prompt = String.format(prompt, chatId, message);
            return prompt;
        }
        if (PromptTypeEnum.GLOBAL != promptType) {
            prompt = String.format(prompt, chatId, message);
            return prompt;
        }
        return prompt;
    }

    /**
     * 构建 ChatClient 请求规范
     */
    private ChatClient.ChatClientRequestSpec getChatClientRequestSpec() {
        String systemPrompt = getPrompt(PromptTypeEnum.GLOBAL, null, null);
        return chatClient.prompt().system(systemPrompt).advisors(new LoggerAdvisor());
    }

    /**
     * 分析用户意图
     */
    private UserIntent analyzeUserIntent(String chatId, String userMessage) {
        log.info("开始分析用户意图");

        String prompt = PromptTypeEnum.USER_INTENT.getPrompt();
        prompt = String.format(prompt, chatId, userMessage);

        UserIntent userIntent = getChatClientRequestSpec()
                .messages(new UserMessage(prompt))
                .call()
                .entity(UserIntent.class);

        log.info("意图识别完成：{}", userIntent.intentType);
        return userIntent;
    }

    /**
     * 用户意图 DTO
     */
    record UserIntent(String intentType, String data) {

    }
}
