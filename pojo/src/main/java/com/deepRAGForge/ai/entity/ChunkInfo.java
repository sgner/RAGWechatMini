package com.deepRAGForge.ai.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkInfo {
    private int chunkIndex; // 分片序号
    private String chunkId; // 分片唯一标识（如 MD5）
    private boolean uploaded; // 是否上传完成
}
