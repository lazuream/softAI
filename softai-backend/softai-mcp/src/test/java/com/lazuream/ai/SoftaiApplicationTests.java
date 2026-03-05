
package com.lazuream.ai;

import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.app.FlowStreamMode;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.lazuream.ai.service.ChatService;
import io.reactivex.Flowable;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SoftaiApplicationTests {

	@Resource
	private ChatService chatService;

	@Autowired
	private ToolCallbackProvider toolCallbackProvider;

	private static final Logger log = LoggerFactory.getLogger(SoftaiApplicationTests.class);


    public static void streamCall()
			throws ApiException, NoApiKeyException, InputRequiredException {
		// 设置 API 基础地址（可选）
		// Constants.baseHttpApiUrl = "https://dashscope.aliyuncs.com/api/v1";

		ApplicationParam param = ApplicationParam.builder()
				// 若没有配置环境变量，可用百炼 API Key 将下行替换为：.apiKey("sk-xxx")。但不建议在生产环境中直接将 API Key 硬编码到代码中，以减少 API Key 泄露风险。
				.apiKey("sk-c4358ded11ee4e389090c30528227ecb")
				.appId("deec5a17c5e64f87ad3c573fc8afe777")
				.flowStreamMode(FlowStreamMode.MESSAGE_FORMAT) // 启用流式输出
				.prompt("你是谁？")
				.build();

		Application application = new Application();

		// 执行流式调用
		Flowable<ApplicationResult> resultFlow = application.streamCall(param);

		// 处理流式结果
		resultFlow.blockingForEach(data -> {
			if (data.getOutput() != null) {
				// 检查是否是流式消息
				if (data.getOutput().getWorkflowMessage() != null
						&& data.getOutput().getWorkflowMessage().getMessage() != null) {
					String content = data.getOutput().getWorkflowMessage().getMessage().getContent();
					if (content != null) {
						System.out.printf("📤 流式内容：%s\n", content);
					}
				}

				// 检查是否结束
				if (data.getOutput().getFinishReason() != null
						&& data.getOutput().getFinishReason().equals("stop")) {
					String text = data.getOutput().getText();
					if (text != null) {
						System.out.printf("\n✅ 任务完成，最终文本：%s\n", text);
					}
				}
			}
		});
	}

	@Test
	void contextLoads() {
		try {
			System.out.println("🚀 开始流式调用百炼工作流...\n");
			streamCall();
		} catch (ApiException | NoApiKeyException | InputRequiredException e) {
			System.err.println("❌ 错误信息：" + e.getMessage());
			System.out.println("请参考文档：https://help.aliyun.com/zh/model-studio/developer-reference/error-code");
		}
		System.exit(0);
	}

	@Test
	void test() {
		chatService.getAiNews();
	}

	@Test
	void testTools() {
		// 调试：查看所有注册的工具
		ToolCallback[] callbacks = toolCallbackProvider.getToolCallbacks();
		log.info("📋 已注册的工具数量：{}", callbacks.length);

		for (ToolCallback callback : callbacks) {
			ToolDefinition def = callback.getToolDefinition();
			log.info("工具 - name: {}, description: {}",
					def.name(), def.description());

			// 检查是否有工具名称为空
			if (def.name() == null || def.name().trim().isEmpty()) {
				log.error("❌ 发现工具名称为空！");
			}
		}
	}

}