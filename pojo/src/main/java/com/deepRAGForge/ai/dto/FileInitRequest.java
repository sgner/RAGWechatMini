package com.deepRAGForge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileInitRequest {
    private String fileId;
    private String fileName;
    private long fileSize;
    private int totalChunks;
}
