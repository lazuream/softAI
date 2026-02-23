package com.lazuream.ai.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

/**
 * @author lazuream
 * @Description
 * @create 2026/2/23
 */
@NoArgsConstructor
@Data
public class MessageVO {
    private String role;
    private String content;

    public MessageVO(Message message) {
        switch (message.getMessageType()) {
            case USER -> this.role = "user";
            case ASSISTANT -> this.role = "assistant";
            default -> throw new IllegalArgumentException("Invalid message type: " + message.getMessageType());
        }
        this.role = role;
        this.content = message.getText();
    }
}
