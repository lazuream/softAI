package com.lazuream.ai.service;


import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.app.FlowStreamMode;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Flowable;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author lazuream
 * @Description
 * @create 2026/3/4
 */
@Service
@Slf4j
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    // 你的工作流应用ID
    private static final String APP_ID = "deec5a17c5e64f87ad3c573fc8afe777";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Tool(name = "aiNewsSearch", description = "查询 ai 热门新闻")
    public String getAiNews() {
        try {
            log.info("----------------------开始获取 AI 新闻----------------------");
            // 从环境变量获取 API Key（更安全）
            String apiKey = System.getenv("DASHSCOPE_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                // 如果环境变量没有，可以 fallback 到直接设置（仅用于测试）
                apiKey = "sk-c4358ded11ee4e389090c30528227ecb";
                log.warn("使用硬编码的 API Key，建议设置 DASHSCOPE_API_KEY 环境变量");
            }

            // 使用流式调用工作流
            StringBuilder fullContent = new StringBuilder();
            streamCall(apiKey, APP_ID, "请汇总近一周 AI 领域的 10 条热门新闻，要求包含标题、摘要和来源链接，按重要性排序。", fullContent);

            log.info("✅ AI 新闻获取成功，结果长度：{}", fullContent.length());
            return fullContent.toString();

        } catch (Exception e) {
            log.error("❌ 获取 AI 新闻失败", e);
            return "获取 AI 新闻失败：" + e.getMessage();
        }
    }

    /**
     * 流式调用工作流（参考测试代码的实现）
     */
    private void streamCall(String apiKey, String appId, String prompt, StringBuilder resultCollector)
            throws ApiException, NoApiKeyException, InputRequiredException {

        log.info("🚀 开始流式调用百炼工作流，appId: {}", appId);

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
                        log.debug("📤 流式内容：{}", content);
                        resultCollector.append(content);
                    }
                }

                // 检查是否结束
                if (data.getOutput().getFinishReason() != null
                        && data.getOutput().getFinishReason().equals("stop")) {
                    String text = data.getOutput().getText();
                    if (text != null) {
                        log.info("\n✅ 任务完成，最终文本：{}", text);
                    }
                }
            }
        });
    }
}
