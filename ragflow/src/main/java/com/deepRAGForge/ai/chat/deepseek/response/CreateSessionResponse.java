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
public class CreateSessionResponse {
     private Integer code;
     private String message;
     private ResponseData data;
     @Data
     @AllArgsConstructor
     @NoArgsConstructor
     @Builder
     public static class ResponseData{
         @JsonProperty("chat_id")
          private String chatId;
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
     @Data
     @AllArgsConstructor
     @NoArgsConstructor
     @Builder
     public static class Message{
          private String content;
          private String role;
     }
}
