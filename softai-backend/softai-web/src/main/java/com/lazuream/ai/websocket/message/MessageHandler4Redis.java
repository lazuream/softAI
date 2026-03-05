package com.lazuream.ai.websocket.message;

import com.lazuream.ai.entity.dto.MessageSendDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageHandler4Redis implements MessageHandler {

    @Override
    public void listenMessage() {
        log.warn("listenMessage() 方法未实现 - WebSocket 功能已禁用");
    }

    @Override
    public void sendMessage(MessageSendDTO sendDto) {
        log.warn("sendMessage() 方法未实现 - WebSocket 功能已禁用. userId={}",
                sendDto.getChatId());
        // 不执行任何操作，只记录日志
    }
}
