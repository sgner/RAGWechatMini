package com.deepRAGForge.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadFileRequest {
    private String fileId;
    private int chunkIndex;
    private String kbName;
    private String kbId;
}
