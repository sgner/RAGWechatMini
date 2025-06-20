package com.deepRAGForge.ai.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "files")
public class FileMetadata {
    @Id
    private String id;
    private String userId; // 用户 ID
    private String fileName;
    private long fileSize;
    private String bucketName; // 存储桶名称
    private Date uploadTime;
    private List<ChunkInfo> chunks;
    private String fileExtension;
}
