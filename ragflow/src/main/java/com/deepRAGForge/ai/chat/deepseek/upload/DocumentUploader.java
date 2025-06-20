package com.deepRAGForge.ai.chat.deepseek.upload;

import com.deepRAGForge.ai.chat.deepseek.response.DocumentUploadResponse;
import com.deepRAGForge.ai.properties.RAGFlowDeepSeekProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
@Component
public class DocumentUploader {

    private final OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(20, 5, TimeUnit.MINUTES))
                    .build();
    private final ObjectMapper objectMapper;
    private final RAGFlowDeepSeekProperties properties;

    public DocumentUploader(ObjectMapper objectMapper, RAGFlowDeepSeekProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    /**
     * 文件上传方法
     * @param datasetId 数据集ID
     * @param files 要上传的文件列表
     * @return 上传结果响应
     */
    public DocumentUploadResponse uploadDocuments(String datasetId, List<File> files)
            throws IOException {

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        // 添加多个文件
        for (File file : files) {
            RequestBody fileBody = RequestBody.create(
                    MediaType.get("application/octet-stream"),
                    file
            );
            bodyBuilder.addFormDataPart("file", file.getName(), fileBody);
        }

        Request request = new Request.Builder()
                .url(properties.getDatasetUrl()+"/"+ datasetId + "/documents")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .post(bodyBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败，状态码: " + response.code());
            }
            return objectMapper.readValue(response.body().string(), DocumentUploadResponse.class);
        }
    }
}
