package hope.smarteditor.user.controller;


import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import hope.smarteditor.common.model.dto.SseChatResponse;
import hope.smarteditor.common.model.dto.StreamRequest;
import hope.smarteditor.user.strategy.TextProcessingStrategy;
import hope.smarteditor.user.strategyFactory.TextProcessingStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import reactor.core.publisher.Flux;
import hope.smarteditor.common.model.dto.ChatRequest;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;

@RestController
@RequestMapping("/ai")
@Slf4j
public class AiController {

   @Resource
        private TextProcessingStrategyFactory strategyFactory;

        @Resource
        private StreamingChatLanguageModel chatClient;

    /**
     * 返回 Flux<ServerSentEvent<SseChatResponse>>，让前端通过 SSE 接收数据
     * produces = MediaType.TEXT_EVENT_STREAM_VALUE 保证返回的是 event-stream
     */
    @PostMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<SseChatResponse>> streamData(@RequestBody StreamRequest streamRequest) {

        // 1. 从请求中提取参数
        String type = streamRequest.getType();
        String userQuery = streamRequest.getUserQuery();

        // 2. 根据 "type" 获取对应策略，生成提示词
        TextProcessingStrategy strategy = strategyFactory.getStrategy(type);
        String prompt = strategy.generatePrompt(userQuery);

        // 3. 构建聊天消息（此处仅做演示，可根据需求定制系统消息等）
        List<ChatMessage> messages = List.of(
                systemMessage("你是一个全能的AI助手"),
                userMessage(prompt)
        );

        // 4. 使用 Flux.create(...) 实现 “流式” 响应
        return Flux.create(emitter -> {
            // 调用 chatClient.generate 进行流式推送
            chatClient.generate(messages, new StreamingResponseHandler<AiMessage>() {

                @Override
                public void onNext(String token) {
                    // 将每次生成的 token 封装为自定义对象 SseChatResponse
                    SseChatResponse sseResponse = new SseChatResponse(token);
                    // 通过 SSE 发送
                    ServerSentEvent<SseChatResponse> event = ServerSentEvent.builder(sseResponse).build();
                    emitter.next(event);
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    // 当生成结束时，调用 complete() 通知下游
                    emitter.complete();
                }

                @Override
                public void onError(Throwable error) {
                    // 出错时，记录日志并调用 emitter.error()
                    log.error("流式输出出错：", error);
                    emitter.error(error);
                }
            });

            // 处理客户端取消订阅的情况（如前端 EventSource.close()）
            emitter.onCancel(() -> {
                log.info("客户端取消订阅");
                // 如果 chatClient 支持取消，可以在这里调用对应的“取消操作”。
            });
        });
    }


    @PostMapping(value = "/test/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<SseChatResponse>> sse(@RequestBody ChatRequest chatRequest) {
        return Flux.create(emitter -> {
            // 从 chatRequest 中读取用户输入
            String userInput = chatRequest.getUserText();

            // 构建 ChatMessage 列表
            // 假设您想指定一个“系统角色”的提示 + 用户输入
            List<ChatMessage> messages = List.of(
                    systemMessage("你是一个语文老师"),
                    userMessage(userInput)
            );

            // 调用 chatClient.generate 来进行流式输出
            chatClient.generate(messages, new StreamingResponseHandler<AiMessage>() {

                @Override
                public void onNext(String token) {
                    // 将每次生成的 token 封装成 SSE 响应
                    SseChatResponse sseResponse = new SseChatResponse(token);
                    ServerSentEvent<SseChatResponse> event = ServerSentEvent.builder(sseResponse).build();
                    emitter.next(event);
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    // 流结束
                    emitter.complete();
                }

                @Override
                public void onError(Throwable error) {
                    log.error("流式输出出错：", error);
                    emitter.error(error);
                }
            });

            // 处理客户端取消订阅的情况
            emitter.onCancel(() -> {
                log.info("客户端取消订阅");
                // 如果 chatClient 支持取消，可以在这里进行取消操作
            });
        });
    }
}

