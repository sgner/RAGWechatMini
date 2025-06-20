package com.deepRAGForge.ai.chat.deepseek.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateAssistantRequest {
    private String name;
    private String avatar;

    @JsonProperty("dataset_ids")
    private List<String> datasetIds;

    private LlmConfig llm;
    private PromptConfig prompt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LlmConfig {
        @JsonProperty("model_name")
        private String modelName;
        private Double temperature;

        @JsonProperty("top_p")
        private Double topP;

        @JsonProperty("presence_penalty")
        private Double presencePenalty;

        @JsonProperty("frequency_penalty")
        private Double frequencyPenalty;

        @JsonProperty("max_token")
        private Integer maxToken;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PromptConfig {
        @JsonProperty("similarity_threshold")
        private Double similarityThreshold;

        @JsonProperty("keywords_similarity_weight")
        private Double keywordsSimilarityWeight;

        @JsonProperty("top_n")
        private Integer topN;

        private List<Variable> variables;

        @JsonProperty("rerank_model")
        private String rerankModel;

        @JsonProperty("top_k")
        private Integer topK;

        @JsonProperty("empty_response")
        private String emptyResponse;

        private String opener;

        @JsonProperty("show_quote")
        private Boolean showQuote;

        private String prompt;
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