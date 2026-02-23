package com.lazuream.ai.repository;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lazuream
 * @Description
 * @create 2026/2/23
 */

@Component
public class InMemoryChatHistoryRepository implements ChatHistoryRepository{

    private final Map<String, List<String> > chatHistoryMap = new HashMap<>();

    @Override
    public void save(String type, String chatId) {
//        if(!chatHistoryMap.containsKey(type)) {
//            chatHistoryMap.put(type, List.of(chatId));
//        }
//        List<String> chatIds = chatHistoryMap.get(type);
        List<String> chatIds = chatHistoryMap.computeIfAbsent(type, k -> new ArrayList<>());
        if(chatIds.contains(chatId)) {
            return;
        }
        chatIds.add(chatId);

        chatHistoryMap.put(type, chatIds);
    }

    @Override
    public List<String> getChatIds(String type) {
//        List<String> chatIds = chatHistoryMap.get(type);
//        return chatIds == null ? new ArrayList<>() : chatIds;
        return chatHistoryMap.getOrDefault(type, new ArrayList<>());
    }
}
