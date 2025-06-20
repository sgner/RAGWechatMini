package com.deepRAGForge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeepSeekApiDto {
    private String chatId;
    private String question;
    private String sessionId;
    private String userId;
}
