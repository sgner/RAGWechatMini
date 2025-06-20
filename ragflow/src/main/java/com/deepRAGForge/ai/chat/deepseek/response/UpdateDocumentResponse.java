package com.deepRAGForge.ai.chat.deepseek.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateDocumentResponse {
    private String message;
    private Integer code;
}
