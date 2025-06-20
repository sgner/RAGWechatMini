package com.deepRAGForge.ai.service;

import com.deepRAGForge.ai.chat.deepseek.response.DocumentUploadResponse;
import com.deepRAGForge.ai.dto.FileInitRequest;
import com.deepRAGForge.ai.entity.ChunkInfo;
import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface FileUploadService {
    public DocumentUploadResponse uploadChunk(String fileId, int chunkIndex, MultipartFile file, String kbName, String kbId) throws Exception;
    public List<ChunkInfo> getChunks(String fileId);
    public void mergeChunks(String userId,String fileId,String kbName,String unid) throws Exception;
    public void initUpload(FileInitRequest fileInitRequest) throws Exception;
}
