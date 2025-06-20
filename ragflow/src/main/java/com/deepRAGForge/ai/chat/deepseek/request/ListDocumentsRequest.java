package com.deepRAGForge.ai.chat.deepseek.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListDocumentsRequest {
    private Integer page = 1;
    @JsonProperty("page_size")
    private Integer pageSize = 10;
    private String orderby = "update_time";
    private Boolean desc = true;
    private String keywords;
    @JsonProperty("document_id")
    private String documentId;
    private String name;
}
