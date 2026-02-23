package com.lazuream.ai.repository;

import java.util.List;

/**
 * @author lazuream
 * @Description
 * @create 2026/2/23
 */
public interface ChatHistoryRepository {

    /**
     * 保存会话记录
     * @param type 服务类型，chat，service
     * @param chatId 会话ID
     */
    void save(String type, String chatId);

    /**
     * 获取会话列表
     * @param type
     * @return 会话ID列表
     */
    List<String> getChatIds(String type);
}
