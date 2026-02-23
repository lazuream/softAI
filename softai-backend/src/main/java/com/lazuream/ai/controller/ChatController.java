package com.lazuream.ai.controller;

import com.lazuream.ai.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

/**
 * @author lazuream
 * @Description
 * @create 2026/2/21
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatController {

    private final ChatClient chatClient;

    private final ChatHistoryRepository chatHistoryRepository;

    /**
     * 聊天
     * @param prompt
     * @param chatId
     * @return
     */
    @RequestMapping(value = "/chat", produces = "text/html;charset=UTF-8")
    public Flux<String> chat(String prompt, String chatId) {
        // 1、保存会话记录
        chatHistoryRepository.save("chat", chatId);
        // 2、发起聊天
        return chatClient.prompt()
                    .user(prompt)
                    .advisors(a -> a.param(CONVERSATION_ID, chatId))
                    .stream()
                    .content();
    }
}
