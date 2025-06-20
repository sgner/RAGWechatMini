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
public class ListChatSessionRequest {
    @JsonProperty("chat_id")
    private String chatId;
    private Integer page=1;
    @JsonProperty("page_size")
    private Integer pageSize=30;
    private String orderby="create_time";
    private Boolean desc=true;
    private String name;
    private String id;
    @JsonProperty("user_id")
    private String userId;

}
