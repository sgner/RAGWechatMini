package com.deepRAGForge.ai.chat.deepseek.response;

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
public class DocumentUploadResponse {
    private Integer code;
    private String message;
    private List<DocumentInfo> data;

    @Data
    public static class DocumentInfo {
        private String chunk_method;
        private String created_by;
        private String dataset_id;
        private String id;
        private String location;
        private String name;
        private ParserConfig parser_config;
        private String run;
        private Long size;
        private String thumbnail;
        private String type;
    }

    @Data
    public static class ParserConfig {
        private Integer chunk_token_num;
        private String delimiter;
        private Boolean html4excel;
        private String layout_recognize;
        private RaptorConfig raptor;
    }

    @Data
    public static class RaptorConfig {
        @JsonProperty("user_raptor")
        private Boolean userRaptor;
    }
}
