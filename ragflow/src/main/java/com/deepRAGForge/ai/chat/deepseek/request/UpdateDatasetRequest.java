package com.deepRAGForge.ai.chat.deepseek.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDatasetRequest {
    private String name;

    @JsonProperty("embedding_model")
    private String embeddingModel;

    @JsonProperty("chunk_method")
    private ChunkMethod chunkMethod;

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
