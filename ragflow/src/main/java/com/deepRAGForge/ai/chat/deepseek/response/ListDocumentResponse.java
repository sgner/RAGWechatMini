package com.deepRAGForge.ai.chat.deepseek.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class  ListDocumentResponse {
    private Integer code;
    private ResponseData data;
    private String message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseData {
        private List<Document> docs;
        private Integer total;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Document {
        private Integer chunk_count;
        private String create_date;
        private String create_time;
        private String created_by;
        private String id;
        private String knowledgebase_id;
        private String location;
        private String name;
        private ParserConfig parser_config;
        private String chunk_method;
        private String process_begin_at;
        private Double process_duation;
        private Double progress;
        private String progress_msg;
        private String run;
        private Integer size;
        private String source_type;
        private String status;
        private String thumbnail;
        private Integer token_count;
        private String type;
        private String update_date;
        private String update_time;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParserConfig {
        @Builder.Default
        private Integer chunk_token_count = 128;
        @Builder.Default
        private String delimiter = "\n!?。；！？";
        @Builder.Default
        private String layout_recognize = "true";
        @Builder.Default
        private Integer task_page_size = 12;
    }
}
