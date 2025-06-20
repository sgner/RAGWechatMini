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
public class ListAssistantsRequest {
    @JsonProperty("page")
    private Integer page = 1 ;
    @JsonProperty("page_size")
    private Integer pageSize = 30;
    private String orderby="update_time";
    private Boolean desc=true;
    private String id;
    private String name;
}