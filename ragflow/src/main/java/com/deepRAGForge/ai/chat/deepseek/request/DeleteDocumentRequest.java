package com.deepRAGForge.ai.chat.deepseek.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DeleteDocumentRequest {
    private List<String> ids;
}
