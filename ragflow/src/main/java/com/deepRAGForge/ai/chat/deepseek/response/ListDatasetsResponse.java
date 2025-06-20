package com.deepRAGForge.ai.chat.deepseek.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListDatasetsResponse {

    private Integer code;

    private String message;

    private List<DatasetInfo> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatasetInfo {
        private String avatar;

        @JsonProperty("chunk_count")
        private Integer chunkCount;

        @JsonProperty("create_date")
        private String createDate;

        @JsonProperty("create_time")
        private Long createTime;

        @JsonProperty("created_by")
        private String createdBy;

        private String description;

        @JsonProperty("document_count")
        private Integer documentCount;

        @JsonProperty("embedding_model")
        private String embeddingModel;

        private String id;
        private String language;
        private String name;

        @JsonProperty("chunk_method")
        private ChunkMethod chunkMethod;

        @JsonProperty("parser_config")
        private ParserConfig parserConfig;

        private String permission;

        @JsonProperty("similarity_threshold")
        private Double similarityThreshold;

        private String status;

        @JsonProperty("tenant_id")
        private String tenantId;

        @JsonProperty("token_num")
        private Integer tokenNum;

        @JsonProperty("update_date")
        private String updateDate;

        @JsonProperty("update_time")
        private Long updateTime;

        @JsonProperty("vector_similarity_weight")
        private Double vectorSimilarityWeight;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParserConfig {
        @JsonProperty("chunk_token_num")
        private Integer chunkTokenNum;

        private String delimiter;

        @JsonProperty("entity_types")
        private List<String> entityTypes;
    }

    /**
     * 分块方法枚举
     */
    public enum ChunkMethod {
        @JsonProperty("naive") NAIVE,
        @JsonProperty("manual") MANUAL,
        @JsonProperty("qa") QA,
        @JsonProperty("table") TABLE,
        @JsonProperty("paper") PAPER,
        @JsonProperty("book") BOOK,
        @JsonProperty("laws") LAWS,
        @JsonProperty("presentation") PRESENTATION,
        @JsonProperty("picture") PICTURE,
        @JsonProperty("one") ONE,
        @JsonProperty("email") EMAIL,
        @JsonProperty("knowledge_graph") KNOWLEDGE_GRAPH
    }
}
