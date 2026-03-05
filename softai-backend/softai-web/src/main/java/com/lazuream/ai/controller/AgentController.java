package com.lazuream.ai.controller;

import com.lazuream.ai.component.ChatComponent;
import com.lazuream.ai.entity.po.AgentMessage;
import com.lazuream.ai.entity.vo.ResponseVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lazuream
 * @Description
 * @create 2026/3/4
 */
@RestController
@RequestMapping("/agent")
@Slf4j
public class AgentController extends ABaseController{

    @Resource
    private ChatComponent chatComponent;

    @RequestMapping("/sendMessage")
    public ResponseVO sendMessage(String chatId, String message) {
        log.info("📨 收到消息：chatId={}, message={}", chatId, message);

        // 同步调用 AI 并获取回复
        String aiResponse = chatComponent.send(chatId, message);

        log.info("✅ AI 回复：{}", aiResponse);

        return getSuccessResponseVO(aiResponse);
    }
}
