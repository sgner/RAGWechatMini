package com.deepRAGForge.ai.chat.deepseek;
import cn.hutool.aop.interceptor.SpringCglibInterceptor;
import com.alibaba.fastjson.JSONObject;
import com.deepRAGForge.ai.chat.deepseek.completionStream.ChatCompletionStream;
import com.deepRAGForge.ai.chat.deepseek.request.*;
import com.deepRAGForge.ai.chat.deepseek.response.*;
import com.deepRAGForge.ai.chat.deepseek.upload.DocumentUploader;
import com.deepRAGForge.ai.chat.deepseek.util.FileConverter;
import com.deepRAGForge.ai.properties.RAGFlowDeepSeekProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import com.deepRAGForge.ai.chat.deepseek.response.RelatedQuestionResponse;
import com.deepRAGForge.ai.chat.deepseek.request.RelatedQuestionRequest;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
@Slf4j
@Component
@RequiredArgsConstructor
public class DeepSeekRAGFlowClient {
    private final ChatCompletionStream chatCompletionStream;
    private final RAGFlowDeepSeekProperties properties;
    private final DocumentUploader documentUploader;
    private final ObjectMapper objectMapper;
    private final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .connectionPool(new ConnectionPool(20, 5, TimeUnit.MINUTES))
            .build();
    public record ChatResponse(
            String answer,
            List<ChatCompletionStream.CompletionChunk.DocAgg> docAggs

    ){}
    public Flux<ChatResponse> stream(String chatId, String question, @Nullable String sessionId, @Nullable String userId) {
        Flux<ChatCompletionStream.CompletionChunk> completionChunkFlux = chatCompletionStream.streamCompletion(chatId, question, sessionId, userId);
        return completionChunkFlux.map(chunk -> {
            Object data = chunk.data();
            if(chunk.code()==0&&data == null&&chunk.message().equals("END")){
                return new ChatResponse(null,null);
            }
            String answer = null;
            List<ChatCompletionStream.CompletionChunk.DocAgg> docAggs = Collections.emptyList();
            if (data instanceof ChatCompletionStream.CompletionChunk.ChunkData completionData) {
                // 安全获取 answer
                answer = completionData.answer() != null ? completionData.answer() : "";

                // 安全获取 reference 和 doc_aggs
                var reference = completionData.reference();
                if (reference != null) {
                    var docAggsList = reference.doc_aggs();
                    docAggs = docAggsList != null ? docAggsList : Collections.emptyList();
                }
            } else {
                answer = "No answer available";
            }
            ChatResponse chatResponse = new ChatResponse(answer, docAggs);
            log.info(chatResponse.toString());
            return new ChatResponse(answer, docAggs);
        });
    }

    public CreateDatasetResponse createDataset(CreateDatasetRequest createDatasetRequest) throws IOException {
        RequestBody requestBody = RequestBody.create(JSONObject.toJSONString(createDatasetRequest), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(properties.getDatasetUrl())
                .post(requestBody)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .build();
        Response response = okHttpClient.newCall(request).execute();
        assert response.body() != null;
        return objectMapper.readValue(response.body().string(), CreateDatasetResponse.class);
    }
    public DeleteDatasetResponse deleteDataset(DeleteDatasetRequest deleteDatasetRequest) throws IOException {
        RequestBody requestBody = RequestBody.create(JSONObject.toJSONString(deleteDatasetRequest),MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(properties.getDatasetUrl())
                .delete(requestBody)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .build();
        Response response = okHttpClient.newCall(request).execute();
        assert response.body() != null;
        return JSONObject.parseObject(response.body().string(), DeleteDatasetResponse.class);
    }
    public UpdateDatasetResponse updateDataset(UpdateDatasetRequest updateDatasetRequest,String datasetId) throws IOException {
        RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(updateDatasetRequest), MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(properties.getDatasetUrl()+"/"+datasetId)
                .put(requestBody)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .build();
        Response response = okHttpClient.newCall(request).execute();
        assert response.body() != null;
        return JSONObject.parseObject(response.body().string(),UpdateDatasetResponse.class);
    }
    public ListDatasetsResponse listDatasets(ListDatasetsRequest listDatasetRequest) throws IOException {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(properties.getDatasetUrl()))
                .newBuilder()
                .addQueryParameter("page",listDatasetRequest.getPage()+"")
                .addQueryParameter("page_size",listDatasetRequest.getPageSize()+"")
                .addQueryParameter("orderby",listDatasetRequest.getOrderby())
                .addQueryParameter("desc",listDatasetRequest.getDesc()+"")
                .addQueryParameter("name",listDatasetRequest.getName())
                .addQueryParameter("id",listDatasetRequest.getId())
                .build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", "Bearer " + properties.getApiKey())
                .build();
        Response response = okHttpClient.newCall(request).execute();
        assert response.body() != null;
        return objectMapper.readValue(response.body().string(),ListDatasetsResponse.class);
    }
public DocumentUploadResponse uploadDocument(List<MultipartFile> multipartFiles,String datasetId) throws IOException {
    List<File> files = FileConverter.convertMultipartFilesToFiles(multipartFiles);
    DocumentUploadResponse documentUploadResponse = documentUploader.uploadDocuments(datasetId, files);
    FileConverter.deleteFiles(files);
    return documentUploadResponse;
}
public UpdateDocumentResponse updateDocument(UpdateDocumentRequest updateDocumentRequest,String dataset_id,String document_id) throws IOException {
    RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(updateDocumentRequest), MediaType.parse("application/json; charset=utf-8"));
    Request request = new Request.Builder()
            .url(properties.getDatasetUrl() + '/' + dataset_id + "/documents/" + document_id)
            .put(requestBody)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + properties.getApiKey())
            .build();
    Response response = okHttpClient.newCall(request).execute();
    assert response.body() != null;
    return objectMapper.readValue(response.body().string(), UpdateDocumentResponse.class);
}
public String downloadDocument(String dataset_id,String document_id) throws IOException {
    Request request = new Request.Builder()
            .url(properties.getDatasetUrl() + "/" + dataset_id + "/documents/" + document_id)
            .get()
            .header("Authorization", "Bearer " + properties.getApiKey())
            .build();
    Response response = okHttpClient.newCall(request).execute();
    assert response.body() != null;
    return response.body().string();
}

    public ListDocumentResponse listDocuments(ListDocumentsRequest listDocumentRequest, String dataset_id) throws IOException {
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(properties.getDatasetUrl() + "/" + dataset_id + "/documents")).newBuilder();

        if (listDocumentRequest.getPage() != null) {
            builder.addQueryParameter("page", listDocumentRequest.getPage().toString());
        }
        if (listDocumentRequest.getPageSize() != null) {
            builder.addQueryParameter("page_size", listDocumentRequest.getPageSize().toString());
        }
        if (listDocumentRequest.getOrderby() != null) {
            builder.addQueryParameter("orderby", listDocumentRequest.getOrderby());
        }
        if (listDocumentRequest.getDesc() != null) {
            builder.addQueryParameter("desc", listDocumentRequest.getDesc().toString());
        }
        if (listDocumentRequest.getKeywords() != null) {
            builder.addQueryParameter("keywords", listDocumentRequest.getKeywords());
        }
        if (listDocumentRequest.getDocumentId() != null) {
            builder.addQueryParameter("id", listDocumentRequest.getDocumentId());
        }
        if (listDocumentRequest.getName() != null) {
            builder.addQueryParameter("name", listDocumentRequest.getName());
        }

        HttpUrl url = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + properties.getApiKey())
                .build();
        Response response = okHttpClient.newCall(request).execute();
        assert response.body() != null;
        return objectMapper.readValue(response.body().string(), ListDocumentResponse.class);
    }

public DeleteDocumentResponse deleteDocument(DeleteDocumentRequest deleteDocumentRequest, String dataset_id) throws IOException {
    RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(deleteDocumentRequest), MediaType.parse("application/json; charset=utf-8"));
    Request request = new Request.Builder()
            .url(properties.getDatasetUrl() + '/' + dataset_id + "/documents")
            .delete(requestBody)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + properties.getApiKey())
            .build();
    Response response = okHttpClient.newCall(request).execute();
    assert response.body() != null;
    return objectMapper.readValue(response.body().string(), DeleteDocumentResponse.class);
   }

   public ParseDocumentResponse parseDocument (ParseDocumentRequest parseDocumentRequest,String dataset_id) throws IOException {
       RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(parseDocumentRequest), MediaType.parse("application/json; charset=utf-8"));
       Request request = new Request.Builder()
               .url(properties.getDatasetUrl() + "/" + dataset_id + "/chunks")
               .post(requestBody)
               .header("Content-Type", "application/json")
               .header("Authorization", "Bearer " + properties.getApiKey())
               .build();
       Response response = okHttpClient.newCall(request).execute();
       assert response.body() != null;
       return objectMapper.readValue(response.body().string(), ParseDocumentResponse.class);
   }

   public ParseDocumentResponse stopParseDocument(ParseDocumentRequest parseDocumentRequest,String dataset_id) throws IOException {
       RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(parseDocumentRequest), MediaType.parse("application/json; charset=utf-8"));
       Request request = new Request.Builder()
               .url(properties.getDatasetUrl() + "/" + dataset_id + "/chunks")
               .delete(requestBody)
               .header("Content-Type", "application/json")
               .header("Authorization", "Bearer " + properties.getApiKey())
               .build();
       Response response = okHttpClient.newCall(request).execute();
       assert response.body() != null;
       return objectMapper.readValue(response.body().string(), ParseDocumentResponse.class);
   }
   public CreateAssistantResponse createAssistantResponse(CreateAssistantRequest createAssistantRequest) throws IOException {
       RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(createAssistantRequest), MediaType.parse("application/json; charset=utf-8"));
       Request request = new Request.Builder()
               .url(properties.getAssistantUrl())
               .post(requestBody)
               .header("Content-Type", "application/json")
               .header("Authorization", "Bearer " + properties.getApiKey())
               .build();
       Response response = okHttpClient.newCall(request).execute();
       assert response.body() != null;
       return objectMapper.readValue(response.body().string(), CreateAssistantResponse.class);
   }
  public UpdateAssistantResponse updateAssistant(UpdateAssistantRequest updateAssistantRequest,String chat_id) throws IOException {
      RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(updateAssistantRequest), MediaType.parse("application/json; charset=utf-8"));
      Request request = new Request.Builder()
              .url(properties.getAssistantUrl() + "/" + chat_id)
              .put(requestBody)
              .header("Content-Type", "application/json")
              .header("Authorization", "Bearer " + properties.getApiKey())
              .build();
      Response response = okHttpClient.newCall(request).execute();
      assert response.body() != null;
      return objectMapper.readValue(response.body().string(), UpdateAssistantResponse.class);
  }
  public DeleteAssistantResponse deleteAssistant(DeleteAssistantRequest deleteAssistantRequest) throws IOException {
      RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(deleteAssistantRequest), MediaType.parse("application/json; charset=utf-8"));
      Request request = new Request.Builder()
              .url(properties.getAssistantUrl())
              .delete(requestBody)
              .header("Content-Type", "application/json")
              .header("Authorization", "Bearer " + properties.getApiKey())
              .build();
      Response response = okHttpClient.newCall(request).execute();
      assert response.body() != null;
      return objectMapper.readValue(response.body().string(), DeleteAssistantResponse.class);
  }
    public ListAssistantResponse listAssistants(ListAssistantsRequest listAssistantsRequest) throws IOException {
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(properties.getAssistantUrl())).newBuilder();
        System.out.println("listAssistants");
        if (listAssistantsRequest.getPage() != null) {
            builder.addQueryParameter("page", listAssistantsRequest.getPage().toString());
        }
        if (listAssistantsRequest.getPageSize() != null) {
            builder.addQueryParameter("page_size", listAssistantsRequest.getPageSize().toString());
        }
        if (listAssistantsRequest.getOrderby() != null) {
            builder.addQueryParameter("orderby", listAssistantsRequest.getOrderby());
        }
        if (listAssistantsRequest.getDesc() != null) {
            builder.addQueryParameter("desc", listAssistantsRequest.getDesc().toString());
        }
        if (listAssistantsRequest.getName() != null) {
            builder.addQueryParameter("name", listAssistantsRequest.getName());
        }
        if (listAssistantsRequest.getId() != null) {
            builder.addQueryParameter("id", listAssistantsRequest.getId());
        }

        HttpUrl url = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", "Bearer " + properties.getApiKey())
                .build();
        Response response = okHttpClient.newCall(request).execute();
        assert response.body() != null;

        return objectMapper.readValue(response.body().string(), ListAssistantResponse.class);
    }

  public CreateSessionResponse createSession(CreateSessionRequest createSessionRequest,String chat_id) throws IOException {
      RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(createSessionRequest), MediaType.parse("application/json; charset=utf-8"));
      Request request = new Request.Builder()
              .url(properties.getAssistantUrl() + "/" + chat_id + "/sessions")
              .post(requestBody)
              .header("Content-Type", "application/json")
              .header("Authorization", "Bearer " + properties.getApiKey())
              .build();
      Response response = okHttpClient.newCall(request).execute();
      assert response.body() != null;
      return objectMapper.readValue(response.body().string(), CreateSessionResponse.class);
  }
public UpdateSessionResponse updateSession(UpdateSessionRequest updateSessionRequest,String chatId,String sessionId) throws IOException {
    RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(updateSessionRequest), MediaType.parse("application/json; charset=utf-8"));
    Request request = new Request.Builder()
            .url(properties.getAssistantUrl() + "/" + chatId + "/" + "sessions" + "/" + sessionId)
            .put(requestBody)
            .header("Authorization", "Bearer " + properties.getApiKey())
            .header("Content-Type", "application/json")
            .build();
    Response response = okHttpClient.newCall(request).execute();
    return objectMapper.readValue(response.body().string(),UpdateSessionResponse.class);
}
    public ListChatSessionResponse listChatSession(ListChatSessionRequest listChatSessionRequest) throws IOException {
        HttpUrl.Builder builder = HttpUrl.parse(properties.getAssistantUrl() + "/" + listChatSessionRequest.getChatId() + "/sessions").newBuilder();

        if (listChatSessionRequest.getPage() != null) {
            builder.addQueryParameter("page", listChatSessionRequest.getPage().toString());
        }
        if (listChatSessionRequest.getPageSize() != null) {
            builder.addQueryParameter("page_size", listChatSessionRequest.getPageSize().toString());
        }
        if (listChatSessionRequest.getOrderby() != null) {
            builder.addQueryParameter("orderby", listChatSessionRequest.getOrderby());
        }
        if (listChatSessionRequest.getDesc() != null) {
            builder.addQueryParameter("desc", listChatSessionRequest.getDesc().toString());
        }
        if(listChatSessionRequest.getName() != null)
        {
           builder.addQueryParameter("name",listChatSessionRequest.getName());
        }
        if (listChatSessionRequest.getId() != null) {
            builder.addQueryParameter("id", listChatSessionRequest.getId());
        }
        if (listChatSessionRequest.getUserId() != null) {
            builder.addQueryParameter("user_id", listChatSessionRequest.getUserId());
        }

        HttpUrl url = builder.build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Authorization", "Bearer " + properties.getApiKey())
                .build();
        Response response = okHttpClient.newCall(request).execute();
        return objectMapper.readValue(response.body().string(), ListChatSessionResponse.class);
    }
public DeleteChatSessionResponse deleteChatSession(DeleteChatSessionRequest deleteChatSessionRequest,String chatId) throws IOException {
    RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(deleteChatSessionRequest), MediaType.parse("application/json; charset=utf-8"));
    Request request = new Request.Builder()
            .url(properties.getAssistantUrl() + "/" + chatId + "/" + "sessions")
            .delete(requestBody)
            .header("Authorization", "Bearer " + properties.getApiKey())
            .build();
    Response response = okHttpClient.newCall(request).execute();
    return objectMapper.readValue(response.body().string(),DeleteChatSessionResponse.class);
}

public RelatedQuestionResponse relatedQuestions(RelatedQuestionRequest relatedQuestionsRequest) throws IOException {
    RequestBody requestBody = RequestBody.create(objectMapper.writeValueAsString(relatedQuestionsRequest), MediaType.parse("application/json; charset=utf-8"));
    Request request = new Request.Builder()
            .url(properties.getConversationUrl())
            .post(requestBody)
            .header("Authorization", "Bearer " + properties.getApiKey())
            .header("Content-Type", "application/json")
            .build();
    Response response = okHttpClient.newCall(request).execute();
    return objectMapper.readValue(response.body().string(),RelatedQuestionResponse.class);
}

}

