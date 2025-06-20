package com.deepRAGForge.ai.chat.deepseek.completionStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

@Slf4j
@Component
public class ChatCompletionStream {

    private static final Predicate<String> SSE_DONE_PREDICATE = s -> {
        try {
            return new ObjectMapper().readTree(s).path("data").asBoolean(false);
        } catch (Exception e) {
            return false;
        }
    };

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // 响应分块数据结构
    public record CompletionChunk(int code, String message, ChunkData data) {
        public record ChunkData(
                String answer,
                Reference reference,
                String audio_binary,
                String id,
                String session_id
        ) {}

        public record Reference(
                int total,
                List<Chunk> chunks,
                List<DocAgg> doc_aggs
        ) {}

        public record Chunk(
                String id,
                String content,
                String document_id,
                String document_name,
                String dataset_id,
                String image_id,
                double similarity,
                double vector_similarity,
                double term_similarity,
                List<String> positions
        ) {}

        public record DocAgg(String doc_name, String doc_id, int count) {}
    }

    public ChatCompletionStream(@Qualifier("deepseekWebClient") WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 发起流式对话请求
     * @param chatId 聊天助手ID
     * @param question 用户问题
     * @param sessionId 会话ID（可选）
     * @param userId 用户ID（可选）
     * @return 流式响应分块
     */
    public Flux<CompletionChunk> streamCompletion(
            String chatId,
            String question,
            String sessionId,
            String userId
    ) {
        AtomicBoolean isStreaming = new AtomicBoolean(true);

        return webClient.post()
                .uri("/{chat_id}/completions", chatId)
                .bodyValue(buildRequestBody(question, sessionId, userId))
                .retrieve()
                .bodyToFlux(String.class)
                .takeUntil(SSE_DONE_PREDICATE)
                .map(content -> {
                    // 先检查是否是结束信号
                    if (SSE_DONE_PREDICATE.test(content)) {
                        return new CompletionChunk(0, "END",null);
                    }
                    return parseChunk(content);
                })
                .doOnNext(chunk -> {
                    if (chunk.code() != 0) {
                        throw new ChatException("API错误: " + chunk.message());
                    }
                })
                .doOnComplete(() -> isStreaming.set(false))
                .onErrorResume(e -> {
                    if (e instanceof ChatException) {
                        return Flux.error(e);
                    }
                    return Flux.error(new ChatException("流式请求失败", e));
                });
    }

    // 构建请求体
    private Map<String, Object> buildRequestBody(String question, String sessionId, String userId) {
        Map<String, Object> body = new HashMap<>();
        body.put("question", question);
        body.put("stream", true);
        if (StringUtils.hasText(sessionId)) {
            body.put("session_id", sessionId);
        } else if (StringUtils.hasText(userId)) {
            body.put("user_id", userId);
        }
        return body;
    }

    // 解析分块数据
    private CompletionChunk parseChunk(String json) {
        try {
            return objectMapper.readValue(json, CompletionChunk.class);
        } catch (Exception e) {
            throw new ChatException("分块解析失败: " + json, e);
        }
    }

    // 自定义异常
    public static class ChatException extends RuntimeException {
        public ChatException(String message) { super(message); }
        public ChatException(String message, Throwable cause) { super(message, cause); }
    }
}
