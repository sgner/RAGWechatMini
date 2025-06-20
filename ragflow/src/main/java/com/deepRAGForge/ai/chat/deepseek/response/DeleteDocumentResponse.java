package com.deepRAGForge.ai.chat.deepseek.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DeleteDocumentResponse {
    private String message;
    private Integer code;
}
