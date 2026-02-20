package com.lazuream.ai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

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

    @RequestMapping("/chat")
    public Flux<String> chat(String prompt) {
        return chatClient.prompt()
                    .user(prompt)
                    .stream()
                    .content();
    }
}
