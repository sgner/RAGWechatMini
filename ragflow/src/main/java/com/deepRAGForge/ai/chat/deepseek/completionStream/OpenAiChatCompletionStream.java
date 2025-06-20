package com.deepRAGForge.ai.chat.deepseek.completionStream;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiStreamFunctionCallingHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

@Component
public class OpenAiChatCompletionStream {
    private static final Predicate<String> SSE_DONE_PREDICATE;
    private final OpenAiStreamFunctionCallingHelper chunkMerger;
    static {
        SSE_DONE_PREDICATE = "[DONE]"::equals;
    }
    private final WebClient deepSeekOpenAiWebClient;
    public OpenAiChatCompletionStream(@Qualifier("deepseekOpenAiWebClient") WebClient deepSeekOpenAiClient){
        this.deepSeekOpenAiWebClient = deepSeekOpenAiClient;
        this.chunkMerger = new OpenAiStreamFunctionCallingHelper();
    }
    public Flux<OpenAiApi.ChatCompletionChunk> chatCompletionStream(OpenAiApi.ChatCompletionRequest request, MultiValueMap<String,String> additionalHttpHeader, String chat_id){
        AtomicBoolean isInsideTool = new AtomicBoolean(false);
        return ((WebClient.RequestBodySpec)((WebClient.RequestBodySpec) deepSeekOpenAiWebClient.post().uri("/{chat_id}/chat/completions",chat_id)).headers(headers ->{
            headers.addAll(additionalHttpHeader);
        })).body(Mono.just(request), OpenAiApi.ChatCompletionRequest.class)
                .retrieve()
                .bodyToFlux(String.class)
                .takeUntil(SSE_DONE_PREDICATE)
                .filter(SSE_DONE_PREDICATE.negate()).map(content ->{
                     return (OpenAiApi.ChatCompletionChunk) ModelOptionsUtils.jsonToObject(content, OpenAiApi.ChatCompletionChunk.class);
                }).map(chunk ->{
                     if(chunkMerger.isStreamingToolFunctionCall(chunk)){
                           isInsideTool.set(true);
                     }
                     return chunk;
                }).windowUntil(chunk ->{
                     if(isInsideTool.get() && chunkMerger.isStreamingToolFunctionCallFinish(chunk)){
                          isInsideTool.set(false);
                          return true;
                     }else{
                          return !isInsideTool.get();
                     }
                }).concatMapIterable(window ->{
                     Mono<OpenAiApi.ChatCompletionChunk> monoChunk = window.reduce(new OpenAiApi.ChatCompletionChunk((String)null, (List<OpenAiApi.ChatCompletionChunk.ChunkChoice>)null, (Long)null, (String)null, (String)null, (String)null, (String) null,(OpenAiApi.Usage) null), chunkMerger::merge);
                     return List.of(monoChunk);
                }).flatMap(mono ->{
                     return mono;
                });
    }
}
