package com.deepRAGForge.ai.chat.deepseek.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteDatasetRequest {
    private List<String> ids;
}
