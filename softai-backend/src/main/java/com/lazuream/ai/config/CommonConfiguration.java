package com.lazuream.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lazuream
 * @Description
 * @create 2026/2/21
 */
@Configuration
public class CommonConfiguration {

    @Bean
    public ChatClient chatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient
                .builder(ollamaChatModel) // 创建一个 ChatClient 对象，并设置模型为 ollamaChatModel
                .defaultAdvisors(new SimpleLoggerAdvisor()) // 配置日志Advisor
                .defaultSystem("你是一只猫，每句话后面都要加个'喵'")
                .build(); // 构建 ChatClient 对象并返回
    }
}
