package com.deepRAGForge.ai.chat.deepseek;
import com.deepRAGForge.ai.chat.deepseek.completionStream.OpenAiChatCompletionStream;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.model.MessageAggregator;
import org.springframework.ai.chat.observation.ChatModelObservationContext;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.chat.observation.ChatModelObservationDocumentation;
import org.springframework.ai.chat.observation.DefaultChatModelObservationConvention;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.Media;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.FunctionCallingOptions;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.common.OpenAiApiConstants;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DeepSeekRAGFlowOpenAIClient {
     private final ToolCallingManager toolCallingManager;
     private final OpenAiChatCompletionStream  openAiChatCompletionStream;
     private final OpenAiChatOptions defaultOptions;
    private final ObservationRegistry observationRegistry;
    private static final ChatModelObservationConvention DEFAULT_OBSERVATION_CONVENTION = new DefaultChatModelObservationConvention();
    private final ChatModelObservationConvention observationConvention;

     public DeepSeekRAGFlowOpenAIClient(OpenAiChatCompletionStream openAiChatCompletionStream, ToolCallingManager toolCallingManager,ObservationRegistry observationRegistry){
          this.toolCallingManager = toolCallingManager;
          this.openAiChatCompletionStream = openAiChatCompletionStream;
          this.defaultOptions = new OpenAiChatOptions();
          this.observationConvention = DEFAULT_OBSERVATION_CONVENTION;
          this.observationRegistry = observationRegistry;
     }
    public Flux<ChatResponse> stream(Prompt prompt,String chat_id) {
        Prompt requestPrompt = this.buildRequestPrompt(prompt);
        return this.streamChat(requestPrompt, (ChatResponse)null,chat_id);
    }
    public Flux<ChatResponse> streamChat(Prompt prompt,ChatResponse previousChatResponse,String chat_id){
         return Flux.deferContextual((contextView)->{
             OpenAiApi.ChatCompletionRequest request = this.createRequest(prompt, true);
             if (request.outputModalities() != null && request.outputModalities().stream().anyMatch((m) -> {
                 return m.equals("audio");
             })) {
                 log.warn("Audio output is not supported for streaming requests. Removing audio output.");
                 throw new IllegalArgumentException("Audio output is not supported for streaming requests.");
             } else if (request.audioParameters() != null) {
                 log.warn("Audio parameters are not supported for streaming requests. Removing audio parameters.");
                 throw new IllegalArgumentException("Audio parameters are not supported for streaming requests.");
             } else {
                 Flux<OpenAiApi.ChatCompletionChunk> completionChunks = openAiChatCompletionStream.chatCompletionStream(request, this.getAdditionalHttpHeaders(prompt),chat_id);
                 ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap<>();
                 ChatModelObservationContext observationContext = ChatModelObservationContext.builder().prompt(prompt).provider(OpenAiApiConstants.PROVIDER_NAME).requestOptions(prompt.getOptions()).build();
                 Observation observation = ChatModelObservationDocumentation.CHAT_MODEL_OPERATION.observation(this.observationConvention, DEFAULT_OBSERVATION_CONVENTION, () -> {
                     return observationContext;
                 }, this.observationRegistry);
                 observation.parentObservation((Observation)contextView.getOrDefault("micrometer.observation", (Object)null)).start();
                 Flux<ChatResponse> chatResponse = completionChunks.map(this::chunkToChatCompletion).switchMap((chatCompletion) -> {
                     return Mono.just(chatCompletion).map((chatCompletion2) -> {
                         try {
                             String id = chatCompletion2.id();
                             List<Generation> generations = chatCompletion2.choices().stream().map((choice) -> {
                                 if (choice.message().role() != null) {
                                     roleMap.putIfAbsent(id, choice.message().role().name());
                                 }

                                 Map<String, Object> metadata = Map.of("id", chatCompletion2.id(), "role", roleMap.getOrDefault(id, ""), "index", choice.index(), "finishReason", choice.finishReason() != null ? choice.finishReason().name() : "", "refusal", StringUtils.hasText(choice.message().refusal()) ? choice.message().refusal() : "");
                                 return this.buildGeneration(choice, metadata, request);
                             }).toList();
                             OpenAiApi.Usage usage = chatCompletion2.usage();
                             Usage currentChatResponseUsage = usage != null ? this.getDefaultUsage(usage) : new EmptyUsage();
                             Usage accumulatedUsage = UsageUtils.getCumulativeUsage((Usage)currentChatResponseUsage, previousChatResponse);
                             return new ChatResponse(generations, this.from(chatCompletion2, (RateLimit)null, accumulatedUsage));
                         } catch (Exception var10) {
                             log.error("Error processing chat completion", var10);
                             return new ChatResponse(List.of());
                         }
                     });
                 }).buffer(2, 1).map((bufferList) -> {
                     ChatResponse firstResponse = (ChatResponse)bufferList.get(0);
                     if (request.streamOptions() != null && request.streamOptions().includeUsage() && bufferList.size() == 2) {
                         ChatResponse secondResponse = (ChatResponse)bufferList.get(1);
                         if (secondResponse != null && secondResponse.getMetadata() != null) {
                             Usage usage = secondResponse.getMetadata().getUsage();
                             if (!UsageUtils.isEmpty(usage)) {
                                 return new ChatResponse(firstResponse.getResults(), this.from(firstResponse.getMetadata(), usage));
                             }
                         }
                     }

                     return firstResponse;
                 });
                 Flux<ChatResponse> var10000 = chatResponse.flatMap((response) -> {
                     if (ToolCallingChatOptions.isInternalToolExecutionEnabled(prompt.getOptions()) && response.hasToolCalls()) {
                         ToolExecutionResult toolExecutionResult = this.toolCallingManager.executeToolCalls(prompt, response);
                         return toolExecutionResult.returnDirect() ? Flux.just(ChatResponse.builder().from(response).generations(ToolExecutionResult.buildGenerations(toolExecutionResult)).build()) : this.streamChat(new Prompt(toolExecutionResult.conversationHistory(), prompt.getOptions()), response,chat_id);
                     } else {
                         return Flux.just(response);
                     }
                 });
                 Objects.requireNonNull(observation);
                 Flux<ChatResponse> flux = var10000.doOnError(observation::error).doFinally((s) -> {
                     observation.stop();
                 }).contextWrite((ctx) -> {
                     return ctx.put("micrometer.observation", observation);
                 });
                 MessageAggregator var11 = new MessageAggregator();
                 Objects.requireNonNull(observationContext);
                 return var11.aggregate(flux, observationContext::setResponse);
             }
         });
    }

    private OpenAiApi.ChatCompletionRequest createRequest(Prompt prompt, boolean stream) {
        List<OpenAiApi.ChatCompletionMessage> chatCompletionMessages = prompt.getInstructions().stream().map((message) -> {
            if (message.getMessageType() != MessageType.USER && message.getMessageType() != MessageType.SYSTEM) {
                if (message.getMessageType() == MessageType.ASSISTANT) {
                    AssistantMessage assistantMessage = (AssistantMessage)message;
                    List<OpenAiApi.ChatCompletionMessage.ToolCall> toolCalls = null;
                    if (!CollectionUtils.isEmpty(assistantMessage.getToolCalls())) {
                        toolCalls = assistantMessage.getToolCalls().stream().map((toolCall) -> {
                            OpenAiApi.ChatCompletionMessage.ChatCompletionFunction function = new OpenAiApi.ChatCompletionMessage.ChatCompletionFunction(toolCall.name(), toolCall.arguments());
                            return new OpenAiApi.ChatCompletionMessage.ToolCall(toolCall.id(), toolCall.type(), function);
                        }).toList();
                    }

                    OpenAiApi.ChatCompletionMessage.AudioOutput audioOutput = null;
                    if (!CollectionUtils.isEmpty(assistantMessage.getMedia())) {
                        Assert.isTrue(assistantMessage.getMedia().size() == 1, "Only one media content is supported for assistant messages");
                        audioOutput = new OpenAiApi.ChatCompletionMessage.AudioOutput(((Media)assistantMessage.getMedia().get(0)).getId(), (String)null, (Long)null, (String)null);
                    }

                    return List.of(new OpenAiApi.ChatCompletionMessage(assistantMessage.getText(), OpenAiApi.ChatCompletionMessage.Role.ASSISTANT, (String)null, (String)null, toolCalls, (String)null, audioOutput));
                } else if (message.getMessageType() == MessageType.TOOL) {
                    ToolResponseMessage toolMessage = (ToolResponseMessage)message;
                    toolMessage.getResponses().forEach((response) -> {
                        Assert.isTrue(response.id() != null, "ToolResponseMessage must have an id");
                    });
                    return toolMessage.getResponses().stream().map((tr) -> {
                        return new OpenAiApi.ChatCompletionMessage(tr.responseData(), OpenAiApi.ChatCompletionMessage.Role.TOOL, tr.name(), tr.id(), (List)null, (String)null, (OpenAiApi.ChatCompletionMessage.AudioOutput)null);
                    }).toList();
                } else {
                    throw new IllegalArgumentException("Unsupported message type: " + String.valueOf(message.getMessageType()));
                }
            } else {
                Object content = message.getText();
                if (message instanceof UserMessage) {
                    UserMessage userMessage = (UserMessage)message;
                    if (!CollectionUtils.isEmpty(userMessage.getMedia())) {
                        List<OpenAiApi.ChatCompletionMessage.MediaContent> contentList = new ArrayList<>(List.of(new OpenAiApi.ChatCompletionMessage.MediaContent(message.getText())));
                        contentList.addAll(userMessage.getMedia().stream().map(this::mapToMediaContent).toList());
                        content = contentList;
                    }
                }

                return List.of(new OpenAiApi.ChatCompletionMessage(content, OpenAiApi.ChatCompletionMessage.Role.valueOf(message.getMessageType().name())));
            }
        }).flatMap(Collection::stream).toList();
        OpenAiApi.ChatCompletionRequest request = new OpenAiApi.ChatCompletionRequest(chatCompletionMessages, stream);
        OpenAiChatOptions requestOptions = (OpenAiChatOptions)prompt.getOptions();
        request = (OpenAiApi.ChatCompletionRequest)ModelOptionsUtils.merge(requestOptions, request, OpenAiApi.ChatCompletionRequest.class);
        List<ToolDefinition> toolDefinitions = this.toolCallingManager.resolveToolDefinitions(requestOptions);
        if (!CollectionUtils.isEmpty(toolDefinitions)) {
            request = (OpenAiApi.ChatCompletionRequest)ModelOptionsUtils.merge(OpenAiChatOptions.builder().tools(this.getFunctionTools(toolDefinitions)).build(), request, OpenAiApi.ChatCompletionRequest.class);
        }

        if (request.streamOptions() != null && !stream) {
            log.warn("Removing streamOptions from the request as it is not a streaming request!");
            request = request.streamOptions((OpenAiApi.ChatCompletionRequest.StreamOptions)null);
        }

        return request;
    }
    private OpenAiApi.ChatCompletionMessage.MediaContent mapToMediaContent(Media media) {
        MimeType mimeType = media.getMimeType();
        if (MimeTypeUtils.parseMimeType("audio/mp3").equals(mimeType)) {
            return new OpenAiApi.ChatCompletionMessage.MediaContent(new OpenAiApi.ChatCompletionMessage.MediaContent.InputAudio(this.fromAudioData(media.getData()), OpenAiApi.ChatCompletionMessage.MediaContent.InputAudio.Format.MP3));
        } else {
            return MimeTypeUtils.parseMimeType("audio/wav").equals(mimeType) ? new OpenAiApi.ChatCompletionMessage.MediaContent(new OpenAiApi.ChatCompletionMessage.MediaContent.InputAudio(this.fromAudioData(media.getData()), OpenAiApi.ChatCompletionMessage.MediaContent.InputAudio.Format.WAV)) : new OpenAiApi.ChatCompletionMessage.MediaContent(new OpenAiApi.ChatCompletionMessage.MediaContent.ImageUrl(this.fromMediaData(media.getMimeType(), media.getData())));
        }
    }
    private List<OpenAiApi.FunctionTool> getFunctionTools(List<ToolDefinition> toolDefinitions) {
        return toolDefinitions.stream().map((toolDefinition) -> {
            OpenAiApi.FunctionTool.Function function = new OpenAiApi.FunctionTool.Function(toolDefinition.description(), toolDefinition.name(), toolDefinition.inputSchema());
            return new OpenAiApi.FunctionTool(function);
        }).toList();
    }
    private String fromAudioData(Object audioData) {
        if (audioData instanceof byte[] bytes) {
            return Base64.getEncoder().encodeToString(bytes);
        } else {
            throw new IllegalArgumentException("Unsupported audio data type: " + audioData.getClass().getSimpleName());
        }
    }
    private String fromMediaData(MimeType mimeType, Object mediaContentData) {
        if (mediaContentData instanceof byte[] bytes) {
            return String.format("data:%s;base64,%s", mimeType.toString(), Base64.getEncoder().encodeToString(bytes));
        } else if (mediaContentData instanceof String text) {
            return text;
        } else {
            throw new IllegalArgumentException("Unsupported media data type: " + mediaContentData.getClass().getSimpleName());
        }
    }
    private MultiValueMap<String, String> getAdditionalHttpHeaders(Prompt prompt) {
        // 合并请求头
        Map<String, String> headers = new HashMap<>(this.defaultOptions.getHttpHeaders());

        if (prompt.getOptions() instanceof OpenAiChatOptions chatOptions) {
            headers.putAll(chatOptions.getHttpHeaders());
        }

        // 转换为MultiValueMap（类型安全）
        return headers.entrySet().stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> List.of(entry.getValue())
                        ),
                        CollectionUtils::toMultiValueMap
                ));
    }
    private OpenAiApi.ChatCompletion chunkToChatCompletion(OpenAiApi.ChatCompletionChunk chunk) {
        List<OpenAiApi.ChatCompletion.Choice> choices = chunk.choices().stream().map((chunkChoice) -> {
            return new OpenAiApi.ChatCompletion.Choice(chunkChoice.finishReason(), chunkChoice.index(), chunkChoice.delta(), chunkChoice.logprobs());
        }).toList();
        return new OpenAiApi.ChatCompletion(chunk.id(), choices, chunk.created(), chunk.model(), chunk.serviceTier(), chunk.systemFingerprint(), "chat.completion", chunk.usage());
    }



    private Generation buildGeneration(OpenAiApi.ChatCompletion.Choice choice, Map<String, Object> metadata, OpenAiApi.ChatCompletionRequest request) {
        List<AssistantMessage.ToolCall> toolCalls = choice.message().toolCalls() == null ? List.of() : choice.message().toolCalls().stream().map((toolCall) -> {
            return new AssistantMessage.ToolCall(toolCall.id(), "function", toolCall.function().name(), toolCall.function().arguments());
        }).toList();
        String finishReason = choice.finishReason() != null ? choice.finishReason().name() : "";
        ChatGenerationMetadata.Builder generationMetadataBuilder = ChatGenerationMetadata.builder().finishReason(finishReason);
        List<Media> media = new ArrayList<>();
        String textContent = choice.message().content();
        OpenAiApi.ChatCompletionMessage.AudioOutput audioOutput = choice.message().audioOutput();
        if (audioOutput != null) {
            String mimeType = String.format("audio/%s", request.audioParameters().format().name().toLowerCase());
            byte[] audioData = Base64.getDecoder().decode(audioOutput.data());
            Resource resource = new ByteArrayResource(audioData);
            Media.builder().mimeType(MimeTypeUtils.parseMimeType(mimeType)).data(resource).id(audioOutput.id()).build();
            media.add(Media.builder().mimeType(MimeTypeUtils.parseMimeType(mimeType)).data(resource).id(audioOutput.id()).build());
            if (!StringUtils.hasText(textContent)) {
                textContent = audioOutput.transcript();
            }

            generationMetadataBuilder.metadata("audioId", audioOutput.id());
            generationMetadataBuilder.metadata("audioExpiresAt", audioOutput.expiresAt());
        }

        AssistantMessage assistantMessage = new AssistantMessage(textContent, metadata, toolCalls, media);
        return new Generation(assistantMessage, generationMetadataBuilder.build());
    }


    private DefaultUsage getDefaultUsage(OpenAiApi.Usage usage) {
        return new DefaultUsage(usage.promptTokens(), usage.completionTokens(), usage.totalTokens(), usage);
    }
    private ChatResponseMetadata from(ChatResponseMetadata chatResponseMetadata, Usage usage) {
        Assert.notNull(chatResponseMetadata, "OpenAI ChatResponseMetadata must not be null");
        ChatResponseMetadata.Builder builder = ChatResponseMetadata.builder().id(chatResponseMetadata.getId() != null ? chatResponseMetadata.getId() : "").usage(usage).model(chatResponseMetadata.getModel() != null ? chatResponseMetadata.getModel() : "");
        if (chatResponseMetadata.getRateLimit() != null) {
            builder.rateLimit(chatResponseMetadata.getRateLimit());
        }

        return builder.build();
    }
    Prompt buildRequestPrompt(Prompt prompt) {
        OpenAiChatOptions runtimeOptions = null;
        if (prompt.getOptions() != null) {
            ChatOptions var5 = prompt.getOptions();
            if (var5 instanceof ToolCallingChatOptions toolCallingChatOptions) {
                runtimeOptions = (OpenAiChatOptions)ModelOptionsUtils.copyToTarget(toolCallingChatOptions, ToolCallingChatOptions.class, OpenAiChatOptions.class);
            } else {
                var5 = prompt.getOptions();
                if (var5 instanceof FunctionCallingOptions functionCallingOptions) {
                    runtimeOptions = (OpenAiChatOptions)ModelOptionsUtils.copyToTarget(functionCallingOptions, FunctionCallingOptions.class, OpenAiChatOptions.class);
                } else {
                    runtimeOptions = (OpenAiChatOptions)ModelOptionsUtils.copyToTarget(prompt.getOptions(), ChatOptions.class, OpenAiChatOptions.class);
                }
            }
        }

        OpenAiChatOptions requestOptions = (OpenAiChatOptions)ModelOptionsUtils.merge(runtimeOptions, this.defaultOptions, OpenAiChatOptions.class);
        if (runtimeOptions != null) {
            requestOptions.setHttpHeaders(this.mergeHttpHeaders(runtimeOptions.getHttpHeaders(), this.defaultOptions.getHttpHeaders()));
            requestOptions.setInternalToolExecutionEnabled((Boolean)ModelOptionsUtils.mergeOption(runtimeOptions.isInternalToolExecutionEnabled(), this.defaultOptions.isInternalToolExecutionEnabled()));
            requestOptions.setToolNames(ToolCallingChatOptions.mergeToolNames(runtimeOptions.getToolNames(), this.defaultOptions.getToolNames()));
            requestOptions.setToolCallbacks(ToolCallingChatOptions.mergeToolCallbacks(runtimeOptions.getToolCallbacks(), this.defaultOptions.getToolCallbacks()));
            requestOptions.setToolContext(ToolCallingChatOptions.mergeToolContext(runtimeOptions.getToolContext(), this.defaultOptions.getToolContext()));
        } else {
            requestOptions.setHttpHeaders(this.defaultOptions.getHttpHeaders());
            requestOptions.setInternalToolExecutionEnabled(this.defaultOptions.isInternalToolExecutionEnabled());
            requestOptions.setToolNames(this.defaultOptions.getToolNames());
            requestOptions.setToolCallbacks(this.defaultOptions.getToolCallbacks());
            requestOptions.setToolContext(this.defaultOptions.getToolContext());
        }

        ToolCallingChatOptions.validateToolCallbacks(requestOptions.getToolCallbacks());
        return new Prompt(prompt.getInstructions(), requestOptions);
    }
    private Map<String, String> mergeHttpHeaders(Map<String, String> runtimeHttpHeaders, Map<String, String> defaultHttpHeaders) {
        HashMap<String, String> mergedHttpHeaders = new HashMap<>(defaultHttpHeaders);
        mergedHttpHeaders.putAll(runtimeHttpHeaders);
        return mergedHttpHeaders;
    }
    private ChatResponseMetadata from(OpenAiApi.ChatCompletion result, RateLimit rateLimit, Usage usage) {
        Assert.notNull(result, "OpenAI ChatCompletionResult must not be null");
        ChatResponseMetadata.Builder builder = ChatResponseMetadata.builder().id(result.id() != null ? result.id() : "").usage(usage).model(result.model() != null ? result.model() : "").keyValue("created", result.created() != null ? result.created() : 0L).keyValue("system-fingerprint", result.systemFingerprint() != null ? result.systemFingerprint() : "");
        if (rateLimit != null) {
            builder.rateLimit(rateLimit);
        }

        return builder.build();
    }
}
