package com.deepRAGForge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeepSeekOpenAiApiDto {
      private String message;
      private String chat_id;
}
