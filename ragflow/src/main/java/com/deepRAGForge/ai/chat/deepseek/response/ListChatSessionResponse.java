package com.deepRAGForge.ai.chat.deepseek.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListChatSessionResponse {
    private Integer code;
    private List<ChatSession> data;
    private String message;

    // 会话类
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatSession {
        private String chat;

        @JsonProperty("create_date")
        private String createDate;
        @JsonProperty("create_time")
        private Long createTime;
        private String id;
        private List<Message> messages;
        private String name;

        @JsonProperty("update_date")
        private String updateDate;
        @JsonProperty("update_time")
        private Long updateTime;
    }

    // 消息类
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String content;
        private String role;
    }
}
