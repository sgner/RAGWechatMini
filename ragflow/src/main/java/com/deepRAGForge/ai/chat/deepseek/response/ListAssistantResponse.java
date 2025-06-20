package com.deepRAGForge.ai.chat.deepseek.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListAssistantResponse {
    private Integer code;
    private String message;
    private List<ChatData> data;

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

        private String description;

        @JsonProperty("do_refer")
        private String doRefer;

        private String id;

        @JsonProperty("datasets")
        private List<ListDatasetsResponse.DatasetInfo> datasets;

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

        private List<Variable> variables;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Variable {
        private String key;
        private Boolean optional;
    }
}
