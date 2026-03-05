package com.lazuream.ai;

import com.lazuream.ai.component.ChatComponent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class SoftaiApplicationTests {

	private static final Logger log = LoggerFactory.getLogger(SoftaiApplicationTests.class);

	@Autowired
	private ToolCallbackProvider toolCallbackProvider;

	@Autowired
	private ChatComponent chatComponent;

	@Test
	public void test() {
		if (toolCallbackProvider == null) {
			log.error("❌ toolCallbackProvider 为 null！MCP 客户端可能未正确初始化");
			log.error("请检查：");
			log.error("1. MCP 服务器是否已启动（端口 8085）");
			log.error("2. application.yml 中的 MCP 配置是否正确");
			log.error("3. spring.ai.mcp.client.enabled 是否为 true");
			return;
		}

		log.info("🔧 已注册的工具数量：{}", toolCallbackProvider.getToolCallbacks().length);
		for (var callback : toolCallbackProvider.getToolCallbacks()) {
			var def = callback.getToolDefinition();
			log.info("   工具 - name: {}, description: {}", def.name(), def.description());
		}
	}

	@Test
    public void testTools() {
		chatComponent.getAiNews();
	}
}
