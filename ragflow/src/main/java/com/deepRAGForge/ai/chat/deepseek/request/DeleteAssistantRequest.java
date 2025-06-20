package com.deepRAGForge.ai.chat.deepseek.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteAssistantRequest {
    private List<String> ids;
}
