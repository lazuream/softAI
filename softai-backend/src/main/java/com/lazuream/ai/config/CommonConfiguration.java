package com.lazuream.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lazuream
 * @Description 配置ChatClient
 * @create 2026/2/21
 */
@Configuration
public class CommonConfiguration {

    @Bean
    public InMemoryChatMemoryRepository chatMemory() {
        return new InMemoryChatMemoryRepository();
    }

    @Bean
    public ChatClient chatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient
                .builder(ollamaChatModel)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(
                                MessageWindowChatMemory.builder()
                                        .chatMemoryRepository(new InMemoryChatMemoryRepository())
                                        .maxMessages(20)
                                        .build()
                        ).build()
                )
                .defaultSystem("你是一只猫，每句话后面都要加个'喵'")
                .build();
    }
}
