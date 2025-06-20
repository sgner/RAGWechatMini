package com.deepRAGForge.ai.contorller;

import com.deepRAGForge.ai.chat.deepseek.DeepSeekRAGFlowClient;
import com.deepRAGForge.ai.chat.deepseek.DeepSeekRAGFlowOpenAIClient;
import com.deepRAGForge.ai.chat.deepseek.request.*;
import com.deepRAGForge.ai.chat.deepseek.response.*;
import com.deepRAGForge.ai.dto.DeepSeekApiDto;
import com.deepRAGForge.ai.dto.DeepSeekOpenAiApiDto;
import com.deepRAGForge.ai.result.R;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.framework.qual.RequiresQualifier;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;

@RestController
@Slf4j
@RequestMapping("/ragflow/api/v1")
@RequiredArgsConstructor
@CrossOrigin(methods = {RequestMethod.DELETE,RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT},allowCredentials = "true",originPatterns = "*")
public class RAGFlowController {
    private final DeepSeekRAGFlowOpenAIClient deepSeekRAGFlowOpenAIClient;
    private final DeepSeekRAGFlowClient deepSeekRAGFlowClient;

    @PostMapping("/deepseek/openai")
    public Flux<String> deepSeekOpenAiApi(@RequestBody DeepSeekOpenAiApiDto dto){
        Flux<ChatResponse> stream = deepSeekRAGFlowOpenAIClient.stream(new Prompt(new UserMessage(dto.getMessage()),OpenAiChatOptions.builder()
        .model("deepseek-r1:1.5b").build()), dto.getChat_id());
        return stream.map(chatResponse -> {
              String content = chatResponse.getResult().getOutput().getText();
              return content != null ? content : "";
        }).filter(content -> !content.isEmpty())
                .doOnComplete(() -> {
                    log.info("Chat stream completed. Total responses logged");
                })
                .doOnError(error -> {
                    log.error("Error in chat stream: {}", error.getMessage());
                })
                .doFinally(signalType -> {
                    log.info("Stream finished with signal: {}", signalType);
                });
    }
    @PostMapping(value = "/deepseek", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<DeepSeekRAGFlowClient.ChatResponse>> deepSeekApi(@RequestBody DeepSeekApiDto dto) {
        log.info("请求：{}", dto.toString());
        return deepSeekRAGFlowClient.stream(dto.getChatId(), dto.getQuestion(), dto.getSessionId(), dto.getUserId())
                .map(response -> ServerSentEvent.<DeepSeekRAGFlowClient.ChatResponse>builder()
                        .id(String.valueOf(response.hashCode())) // 事件 ID
                        .event("message") // 事件类型
                        .data(response) // 数据
                        .build())
                .mergeWith(Flux.interval(Duration.ofSeconds(15)) // 心跳，每 15 秒
                        .map(i -> ServerSentEvent.<DeepSeekRAGFlowClient.ChatResponse>builder()
                                .comment("keep-alive") // 心跳注释
                                .build()))
                .onErrorResume(e -> Flux.just(ServerSentEvent.<DeepSeekRAGFlowClient.ChatResponse>builder()
                        .event("error")
                        .data(new DeepSeekRAGFlowClient.ChatResponse("Error: " + e.getMessage(), Collections.emptyList()))
                        .build()));
    }
    @PostMapping("/create/dataset")
    public R createDataset(@RequestBody CreateDatasetRequest request) throws IOException {
        CreateDatasetResponse response = deepSeekRAGFlowClient.createDataset(request);
        return  R.success(response);
    }
    @PutMapping("/update/dataset/{id}")
    public R updateDataset(@RequestBody UpdateDatasetRequest request,@PathVariable("id") String id) throws IOException {
        UpdateDatasetResponse updateDatasetResponse = deepSeekRAGFlowClient.updateDataset(request, id);
        return R.success(updateDatasetResponse);
    }
    @PostMapping("/list/dataset")
    public R listDatasets(@RequestBody ListDatasetsRequest request) throws IOException {
        ListDatasetsResponse listDatasetsResponse = deepSeekRAGFlowClient.listDatasets(request);
        return R.success(listDatasetsResponse);
    }
    @DeleteMapping("/delete/dataset")
    public R deleteDataset(@RequestBody DeleteDatasetRequest request) throws IOException {
        DeleteDatasetResponse deleteDatasetResponse = deepSeekRAGFlowClient.deleteDataset(request);
        return R.success(deleteDatasetResponse);
    }
    @PutMapping("/update/document/{dataset_id}/{document_id}")
    public R updateDocument(@RequestBody UpdateDocumentRequest request, @PathVariable("dataset_id") String datasetId,@PathVariable("document_id") String documentId) throws IOException {
        UpdateDocumentResponse updateDocumentResponse = deepSeekRAGFlowClient.updateDocument(request, datasetId, documentId);
        return R.success(updateDocumentResponse);
    }
    @GetMapping("/download/document/{dataset_id}/{document_id}")
    public R downloadDocument(@PathVariable("dataset_id") String datasetId,@PathVariable("document_id") String documentId) throws IOException {
        String s = deepSeekRAGFlowClient.downloadDocument(datasetId, documentId);
        return R.success(s);
    }
    @PostMapping("/list/document/{dataset_id}")
    public R listDocument(@RequestBody ListDocumentsRequest request, @PathVariable("dataset_id") String datasetId) throws IOException {
        log.info(datasetId);
        log.info(request.toString());
        ListDocumentResponse listDocumentResponse = deepSeekRAGFlowClient.listDocuments(request, datasetId);
        return R.success(listDocumentResponse);
    }
    @DeleteMapping("/delete/document/{dataset_id}")
    public R deleteDocument(@RequestBody DeleteDocumentRequest request,@PathVariable("dataset_id") String datasetId) throws IOException {
        DeleteDocumentResponse deleteDocumentResponse = deepSeekRAGFlowClient.deleteDocument(request, datasetId);
        return R.success(deleteDocumentResponse);
    }
    @PostMapping("/parse/document/{dataset_id}")
    public R parseDocument(@RequestBody ParseDocumentRequest request, @PathVariable("dataset_id") String datasetId) throws IOException {
        ParseDocumentResponse parseDocumentResponse = deepSeekRAGFlowClient.parseDocument(request, datasetId);
        return R.success(parseDocumentResponse);
    }

    @PostMapping ("/parse/document/stop/{dataset_id}")
    public R stopParseDocument(@RequestBody ParseDocumentRequest request ,@PathVariable("dataset_id") String datasetId) throws IOException {
        ParseDocumentResponse parseDocumentResponse = deepSeekRAGFlowClient.stopParseDocument(request, datasetId);
        return R.success(parseDocumentResponse);
    }
    @PostMapping("/create/assistant")
    public R createAssistantResponse(@RequestBody CreateAssistantRequest request) throws IOException {
        log.info(request.toString());
        CreateAssistantResponse assistantResponse = deepSeekRAGFlowClient.createAssistantResponse(request);
        return R.success(assistantResponse);
    }
    @PostMapping("/update/assistant/{chat_id}")
    public R updateAssistant(@RequestBody UpdateAssistantRequest request,@PathVariable("chat_id") String chatId) throws IOException {
        UpdateAssistantResponse updateAssistantResponse = deepSeekRAGFlowClient.updateAssistant(request, chatId);
        return R.success(updateAssistantResponse);
    }
    @DeleteMapping("/delete/assistant")
    public R deleteAssistant(@RequestBody DeleteAssistantRequest request) throws IOException {
        DeleteAssistantResponse deleteAssistantResponse = deepSeekRAGFlowClient.deleteAssistant(request);
        return R.success(deleteAssistantResponse);
    }
    @PostMapping("/list/assistant")
    public R listAssistants(@RequestBody ListAssistantsRequest request) throws IOException {
        System.out.println("listAssistant");
        ListAssistantResponse listAssistantResponse = deepSeekRAGFlowClient.listAssistants(request);
        return R.success(listAssistantResponse);
    }
    @PostMapping("/create/session/{chat_id}")
    public R createSession(@RequestBody CreateSessionRequest request,@PathVariable("chat_id") String chatId) throws IOException {
        CreateSessionResponse session = deepSeekRAGFlowClient.createSession(request, chatId);
        return R.success(session);
    }
    @PostMapping("/update/session/{chat_id}/{session_id}")
    public R updateSession(@RequestBody UpdateSessionRequest request, @PathVariable("chat_id") String chatId,@PathVariable("session_id") String sessionId) throws IOException {
        UpdateSessionResponse updateSessionResponse = deepSeekRAGFlowClient.updateSession(request, chatId, sessionId);
        return R.success(updateSessionResponse);
    }
    @PostMapping("/list/session")
    public R listSession(@RequestBody ListChatSessionRequest request) throws IOException {
        log.info(request.toString());
        ListChatSessionResponse listChatSessionResponse = deepSeekRAGFlowClient.listChatSession(request);
        return R.success(listChatSessionResponse);
    }
    @PostMapping("/delete/session/{chat_id}")
    public R deleteChatSession(@RequestBody DeleteChatSessionRequest request, @PathVariable("chat_id") String chatId) throws IOException {
        DeleteChatSessionResponse deleteChatSessionResponse = deepSeekRAGFlowClient.deleteChatSession(request, chatId);
        return R.success(deleteChatSessionResponse);
    }
    @PostMapping("/relateQuestions")
    public R relateQuestions(@RequestBody RelatedQuestionRequest request) throws IOException {
        RelatedQuestionResponse relatedQuestionResponse = deepSeekRAGFlowClient.relatedQuestions(request);
        return R.success(relatedQuestionResponse);
    }
}
