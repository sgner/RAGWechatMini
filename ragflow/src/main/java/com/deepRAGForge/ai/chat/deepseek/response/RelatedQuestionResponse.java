package com.deepRAGForge.ai.chat.deepseek.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RelatedQuestionResponse {
    private Integer code;
    private List<String> data;
    private String message;
}
