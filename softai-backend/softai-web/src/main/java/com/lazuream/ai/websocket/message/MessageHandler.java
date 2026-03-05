package com.lazuream.ai.websocket.message;


import com.lazuream.ai.entity.dto.MessageSendDTO;
import org.springframework.stereotype.Component;

@Component("messageHandler")
public interface MessageHandler {

    void listenMessage();

    void sendMessage(MessageSendDTO sendDto);
}
