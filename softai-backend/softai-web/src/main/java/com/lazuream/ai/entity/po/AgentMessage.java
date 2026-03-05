package com.lazuream.ai.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;


/**
 * 
 */
@Data
public class AgentMessage implements Serializable {


	/**
	 * 消息ID
	 */
	private Integer messageId;

	/**
	 * AI消息
	 */
	private String assistantMessage;

	/**
	 * 用户消息
	 */
	private String userMessage;

	/**
	 * 会话ID
	 */
	private String chatId;


	public void setMessageId(Integer messageId){
		this.messageId = messageId;
	}

	public Integer getMessageId(){
		return this.messageId;
	}

	public void setAssistantMessage(String assistantMessage){
		this.assistantMessage = assistantMessage;
	}

	public String getAssistantMessage(){
		return this.assistantMessage;
	}

	public void setUserMessage(String userMessage){
		this.userMessage = userMessage;
	}

	public String getUserMessage(){
		return this.userMessage;
	}


	@Override
	public String toString (){
		return "消息ID:"+(messageId == null ? "空" : messageId)+"，AI消息:"+(assistantMessage == null ? "空" : assistantMessage)+"，用户消息:"+(userMessage == null ? "空" : userMessage);
	}
}
