package com.deepRAGForge.ai.service.impl;

import cn.hutool.core.util.IdUtil;
import com.deepRAGForge.ai.chat.deepseek.DeepSeekRAGFlowClient;
import com.deepRAGForge.ai.chat.deepseek.response.DocumentUploadResponse;
import com.deepRAGForge.ai.dto.FileInitRequest;
import com.deepRAGForge.ai.entity.ChunkInfo;
import com.deepRAGForge.ai.entity.FileMetadata;
import com.deepRAGForge.ai.properties.MinioProperties;
import com.deepRAGForge.ai.service.FileUploadService;
import com.deepRAGForge.ai.utils.ThreadLocalUtil;
import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileUploadServiceImpl implements FileUploadService {
    private final MongoTemplate mongoTemplate;
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final DeepSeekRAGFlowClient dsRAGFlowClient;
    @Override
    public DocumentUploadResponse uploadChunk(String fileId,int chunkIndex,MultipartFile chunk,String kbName,String kbId) throws Exception {
        ensureBucketExists();
        FileMetadata file = mongoTemplate.findById(fileId, FileMetadata.class);
        if(file == null){
           return DocumentUploadResponse.builder().code(101).message("文件不存在").build();
        }
        String chunkId = file.getChunks().get(chunkIndex).getChunkId();
        try{
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(getBucketName())
                            .object(kbName+"/"+chunkId)
                            .stream(chunk.getInputStream(),chunk.getSize(),-1)
                            .build()
            );
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("上传异常");
            return DocumentUploadResponse.builder().code(101).message("片段"+chunkId+"上传异常").build();
            //TODO 日志
        }
        System.out.println(file.getUserId().equals(ThreadLocalUtil.get()));
        if(file.getUserId().equals(ThreadLocalUtil.get())){
            file.getChunks().get(chunkIndex).setUploaded(true);
            mongoTemplate.save(file);
            if (file.getChunks().stream().allMatch(ChunkInfo::isUploaded)) {
                String unid = IdUtil.simpleUUID();
                mergeChunks(file.getUserId(),fileId,kbName,unid);
                return upload2RagFlow(kbName,unid, file, kbId);
            }
        }
        return DocumentUploadResponse.builder().code(0).build();
    }

    @Override
    public List<ChunkInfo> getChunks(String fileId) {
        FileMetadata byId = mongoTemplate.findById(fileId, FileMetadata.class);
        if(byId == null) return null;
        return byId.getChunks();
    }

    @Override
    public void mergeChunks(String userId,String fileId,String kbName,String unid) throws Exception {
      try{
          FileMetadata file = mongoTemplate.findById(fileId, FileMetadata.class);
          if (!file.getUserId().equals(userId)) {
              throw new Exception("Not all chunks are uploaded or access denied");
          }

          String bucketName = getBucketName();
          List<ComposeSource> sources = file.getChunks().stream()
                  .map(chunk -> ComposeSource.builder()
                          .bucket(bucketName)
                          .object(kbName + "/" + chunk.getChunkId())
                          .build())
                  .collect(Collectors.toList());

          minioClient.composeObject(
                  ComposeObjectArgs.builder()
                          .bucket(bucketName)
                          .object(kbName + "/" + file.getFileName()+"-"+unid+file.getFileExtension())
                          .sources(sources)
                          .build()
          );
          System.out.println("合并成功");

          // 删除临时分片
          for (ChunkInfo chunk : file.getChunks()) {
              minioClient.removeObject(
                      RemoveObjectArgs.builder()
                              .bucket(bucketName)
                              .object(kbName +"/" + chunk.getChunkId())
                              .build()
              );
          }
      }  catch (Exception e){
          System.out.println("合并异常");
          e.printStackTrace();
      }

    }

    public void initUpload(FileInitRequest fileInitRequest) throws Exception {
        ensureBucketExists();
        String fileExtension = FilenameUtils.getExtension(fileInitRequest.getFileName());
        String suffix = fileExtension.isEmpty() ? "" : "." + fileExtension;

        FileMetadata.FileMetadataBuilder fileMetadataBuilder = FileMetadata.builder()
                .id(fileInitRequest.getFileId())
                .userId(ThreadLocalUtil.get())
                .bucketName(getBucketName())
                .fileName(fileInitRequest.getFileName())
                .fileExtension(suffix) // 保存文件后缀
                .fileSize(fileInitRequest.getFileSize())
                .uploadTime(new Date());

        ArrayList<ChunkInfo> chunkInfos = new ArrayList<>();
        for (int i = 0; i < fileInitRequest.getTotalChunks(); i++) {
            // 添加后缀到分片ID
            chunkInfos.add(new ChunkInfo(i, fileInitRequest.getFileId() + "-" + i + suffix, false));
        }
        FileMetadata fileMetadata = fileMetadataBuilder.chunks(chunkInfos).build();
        mongoTemplate.save(fileMetadata);
    }

    private String getBucketName(){
        return minioProperties.getBucketPrefix() + "-" + ThreadLocalUtil.get();
    }
    private void ensureBucketExists() throws Exception {
        String bucketName = getBucketName();
        if(!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())){
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            setBucketPolicy(ThreadLocalUtil.get(),bucketName);
        }
    }
    // IAM
    private void setBucketPolicy(String userId, String bucketName) throws Exception {
        String policy = String.format("""
    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Principal": {"AWS": ["user/%s"]},
                "Action": [
                    "s3:GetObject",
                    "s3:PutObject",
                    "s3:DeleteObject",
                    "s3:ListBucket"
                ],
                "Resource": [
                    "arn:aws:s3:::%s/*",
                    "arn:aws:s3:::%s"\s
                ]
            }
        ]
    }
    """, userId, bucketName, bucketName);

        minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policy)
                        .build()
        );
    }
    private DocumentUploadResponse upload2RagFlow(String kbName,String unid,FileMetadata file,String kbId) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String fileName = file.getFileName()+"-"+unid+file.getFileExtension();
        GetObjectResponse object = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(getBucketName())
                        .object(kbName + "/" + fileName)
                        .build()
        );
        byte[] bytes = object.readAllBytes();
        String contentType = object.headers().get("Content-Type");
        MockMultipartFile mockMultipartFile = new MockMultipartFile(fileName, fileName, contentType, bytes);
        return dsRAGFlowClient.uploadDocument(List.of(mockMultipartFile), kbId);
    }

}
