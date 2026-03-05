package com.lazuream.ai.config;

import com.lazuream.ai.service.ChatService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lazuream
 * @Description
 * @create 2026/3/4
 */
@Configuration
public class ServerRegisterConfig {

    @Bean
    public ToolCallbackProvider mcpToolCallbacks(ChatService chatService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(chatService).build();
    }

}
