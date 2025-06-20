package com.deepRAGForge.ai.chat.deepseek.request;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDocumentRequest {
    private String name;
    private Map<String, Object> metaFields;
    private ChunkMethod chunkMethod;
    private ParserConfig parserConfig;

    // 策略接口
    public interface ParserConfig {
        default void validate() throws IllegalArgumentException {}
    }

    // 枚举类型定义chunk_method
    public enum ChunkMethod {
        NAIVE("naive"), MANUAL("manual"), QA("qa"), TABLE("table"),
        PAPER("paper"), BOOK("book"), LAWS("laws"), PRESENTATION("presentation"),
        PICTURE("picture"), ONE("one"), EMAIL("email"), KNOWLEDGE_GRAPH("knowledge_graph");

        private final String value;

        ChunkMethod(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static ChunkMethod fromValue(String value) {
            return Arrays.stream(values())
                    .filter(e -> e.value.equalsIgnoreCase(value))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid chunk method: " + value));
        }
    }

    // 基础配置（空对象）
    @Data
    @NoArgsConstructor
    public static class EmptyConfig implements ParserConfig {
        public static final EmptyConfig INSTANCE = new EmptyConfig();
    }

    // Naive策略配置
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonTypeName("naive")
    public static class NaiveParserConfig implements ParserConfig {
        @Builder.Default
        private Integer chunkTokenCount = 128;

        @Builder.Default
        @JsonProperty("layout_recognize")
        private Boolean layoutRecognize = true;

        @Builder.Default
        @JsonProperty("html4excel")
        private Boolean html4Excel = false;

        @Builder.Default
        private String delimiter = "\n!?。；！？";

        @Builder.Default
        @JsonProperty("task_page_size")
        private Integer taskPageSize = 12;

        @Builder.Default
        private RaptorConfig raptor = new RaptorConfig(false);
    }

    // Raptor策略配置（通用）
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RaptorParserConfig implements ParserConfig {
        @Builder.Default
        private RaptorConfig raptor = new RaptorConfig(false);
    }

    // 知识图谱策略配置
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonTypeName("knowledge_graph")
    public static class KnowledgeGraphConfig implements ParserConfig {
        @Builder.Default
        private Integer chunkTokenCount = 128;

        @Builder.Default
        private String delimiter = "\n!?。；！？";

        @Builder.Default
        @JsonProperty("entity_types")
        private List<String> entityTypes = Arrays.asList(
                "organization", "person", "location", "event", "time"
        );
    }

    // Raptor配置子项
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RaptorConfig {
        @JsonProperty("use_raptor")
        private Boolean useRaptor;
    }

    // 自定义反序列化处理器
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "chunk_method")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = NaiveParserConfig.class, name = "naive"),
            @JsonSubTypes.Type(value = RaptorParserConfig.class, names = {
                    "qa", "manual", "paper", "book", "laws", "presentation"
            }),
            @JsonSubTypes.Type(value = EmptyConfig.class, names = {
                    "table", "picture", "one", "email"
            }),
            @JsonSubTypes.Type(value = KnowledgeGraphConfig.class, name = "knowledge_graph")
    })
    @SuppressWarnings("unused")
    private interface ParserConfigMixIn {}
}
