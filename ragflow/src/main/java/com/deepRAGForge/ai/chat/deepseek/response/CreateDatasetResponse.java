package com.deepRAGForge.ai.chat.deepseek.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDatasetResponse {
    private int code;        // 响应状态码
    private String message;  // 失败时的错误信息，可为 null
    private Dataset data;    // 成功时的嵌套数据，可为 null

    // 成功响应中的 data 结构
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dataset {
        private String avatar;
        @JsonProperty("chunk_count")
        private int chunkCount;
        @JsonProperty("chunk_method")
        private String chunkMethod;
        @JsonProperty("create_date")
        private String createDate;
        @JsonProperty("create_time")
        private long createTime;
        @JsonProperty("created_by")
        private String createdBy;
        private String description;
        @JsonProperty("document_count")
        private int documentCount;
        @JsonProperty("embedding_model")
        private String embeddingModel;
        private String id;
        private String language;
        private String name;
        @JsonProperty("parser_config")
        private ParserConfig parserConfig;
        private String permission;
        @JsonProperty("similarity_threshold")
        private float similarityThreshold;
        private String status;
        @JsonProperty("tenant_id")
        private String tenantId;
        @JsonProperty("token_num")
        private int tokenNum;
        @JsonProperty("update_date")
        private String updateDate;
        @JsonProperty("update_time")
        private long updateTime;
        @JsonProperty("vector_similarity_weight")
        private float vectorSimilarityWeight;
    }

    // parser_config 嵌套结构
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParserConfig {
        @JsonProperty("chunk_token_num")
        private int chunkTokenNum;
        private String delimiter;
        private boolean html4excel;
        @JsonProperty("layout_recognize")
        private boolean layoutRecognize;
        private Raptor raptor;
    }

    // raptor 嵌套结构
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Raptor {
        @JsonProperty("user_raptor")
        private boolean userRaptor;
    }
}