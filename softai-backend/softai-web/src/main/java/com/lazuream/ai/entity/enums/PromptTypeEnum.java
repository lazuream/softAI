package com.lazuream.ai.entity.enums;

import com.lazuream.ai.utils.StringTools;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum PromptTypeEnum {

    GLOBAL("global", """
            # 角色与核心目标
            你是智能助手AI，负责为用户提供服务。
            """, "全局系统提示词"),

    USER_INTENT("userIntent", """
            请分析用户消息的意图,并提取关键信息。
            
            会话ID：%s
            用户问题: %s
            
            请从以下意图类型中选择最匹配的一个：
            INTENT TYPES:
            - AI_NEWS_SEARCH: 搜索ai相关新闻，比如ai最近的新闻有哪些，ai最近的发展怎么样
            - CHAT: 一般性问题，比如 问候、打招呼，商品对比，购物细则等
            
            返回JSON格式，包含以下字段：
            - intentType: 意图类型（使用上面的类型值）
            - data: 关键信息
            示例响应：
            {
              "intentType": "CHAT",
              "data":"你好，我是lm助手"
            }
            """, "用户意图提示词"),
    AI_NEWS_SEARCH("ai_news_search", """
             **调用工具进行ai新闻查询**
             - 根据工具返回结果，友好告知用户结果。
            """, "查询ai热门新闻"),
    CHAT("chat", """
             请基于以下知识库内容回答用户问题，保持友好专业：
             回答要求：
             2. 回答要专业、友好、简洁
             3. 对于政策类问题，需要明确说明条件和限制
             4. 如果涉及多个方面，请分点说明
             会话ID：%s
 
             用户问题:%s
            
            """, "聊天"),
    ;

    private String key;
    private String prompt;
    private String desc;

    PromptTypeEnum(String key, String prompt, String desc) {
        this.key = key;
        this.prompt = prompt;
        this.desc = desc;
    }

    public String getKey() {
        return key;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getDesc() {
        return desc;
    }

    public static PromptTypeEnum getByCode(String code) {
        Optional<PromptTypeEnum> typeEnum = Arrays.stream(PromptTypeEnum.values()).filter(value -> value.toString().equals(code)).findFirst();
        return typeEnum == null || typeEnum.isEmpty() ? null : typeEnum.get();
    }

    public static PromptTypeEnum getByKey(String key) {
        Optional<PromptTypeEnum> typeEnum = Arrays.stream(PromptTypeEnum.values()).filter(value -> value.getKey().equals(key)).findFirst();
        return typeEnum == null || typeEnum.isEmpty() ? null : typeEnum.get();
    }


    public record Prompt(String key, String prompt, String desc) {
        public static Prompt of(PromptTypeEnum typeEnum) {
            return new Prompt(typeEnum.getKey(), typeEnum.getPrompt(), typeEnum.getDesc());
        }
    }

    public static List<Prompt> getPrompts() {
        return Arrays.stream(PromptTypeEnum.values()).filter(value -> !StringTools.isEmpty(value.getPrompt())).map(Prompt::of).toList();
    }
}
