package com.deepRAGForge.ai.chat.deepseek.response;

import com.deepRAGForge.ai.chat.deepseek.request.CreateAssistantRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAssistantResponse {
    private Integer code;
    private String message;
    private ChatData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatData {
        private String avatar;

        @JsonProperty("create_date")
        private String createDate;

        @JsonProperty("create_time")
        private Long createTime;

        @JsonProperty("dataset_ids")
        private List<String> datasetIds;

        private String description;

        @JsonProperty("do_refer")
        private String doRefer;

        private String id;
        private String language;

        private LlmResponse llm;
        private String name;

        private PromptResponse prompt;

        @JsonProperty("prompt_type")
        private String promptType;

        private String status;

        @JsonProperty("tenant_id")
        private String tenantId;

        @JsonProperty("top_k")
        private Integer topK;

        @JsonProperty("update_date")
        private String updateDate;

        @JsonProperty("update_time")
        private Long updateTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LlmResponse {
        @JsonProperty("frequency_penalty")
        private Double frequencyPenalty;

        @JsonProperty("max_tokens")
        private Integer maxTokens;

        @JsonProperty("model_name")
        private String modelName;

        @JsonProperty("presence_penalty")
        private Double presencePenalty;

        private Double temperature;

        @JsonProperty("top_p")
        private Double topP;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptResponse {
        @JsonProperty("empty_response")
        private String emptyResponse;

        @JsonProperty("keywords_similarity_weight")
        private Double keywordsSimilarityWeight;

        private String opener;
        private String prompt;

        @JsonProperty("rerank_model")
        private String rerankModel;

        @JsonProperty("similarity_threshold")
        private Double similarityThreshold;

        @JsonProperty("top_n")
        private Integer topN;

        private List<CreateAssistantRequest.Variable> variables;
    }
}
