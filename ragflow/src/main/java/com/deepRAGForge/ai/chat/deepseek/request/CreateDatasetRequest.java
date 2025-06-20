package com.deepRAGForge.ai.chat.deepseek.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateDatasetRequest {
       @NonNull
       private String name;
       private String avatar;
       private String description;
       @Builder.Default
       private Language language = Language.CHINESE;
       private String embedding_model;
       @Builder.Default
       private Permission permission = Permission.ME;
       @Builder.Default
       private ChunkMethod chunk_method = ChunkMethod.NAIVE;
       @Builder.Default
       private ParserConfig parser_config = new ParserConfig();

       public enum Language {
              ENGLISH("English"),
              CHINESE("Chinese");

              private final String value;

              Language(String value) {
                     this.value = value;
              }

              @JsonProperty
              public String getValue() {
                     return value;
              }
       }

       public enum Permission {
              ME("me"),
              TEAM("team");

              private final String value;

              Permission(String value) {
                     this.value = value;
              }

              @JsonProperty
              public String getValue() {
                     return value;
              }
       }

       public enum ChunkMethod {
              NAIVE("naive"),
              MANUAL("manual"),
              QA("qa"),
              TABLE("table"),
              PAPER("paper"),
              BOOK("book"),
              LAWS("laws"),
              PRESENTATION("presentation"),
              PICTURE("picture"),
              ONE("one"),
              KNOWLEDGE_GRAPH("knowledge_graph"),
              EMAIL("email");

              private final String value;

              ChunkMethod(String value) {
                     this.value = value;
              }

              @JsonProperty
              public String getValue() {
                     return value;
              }
       }

       @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
       @Data
       @Builder
       @AllArgsConstructor
       @NoArgsConstructor
       public static class ParserConfig {
              @Builder.Default
              private Integer chunkTokenCount = 128;
              @Builder.Default
              private Boolean layoutRecognize = true;
              @Builder.Default
              private Boolean html4excel = false;
              @Builder.Default
              private String delimiter = "\n!?。；！？";
              @Builder.Default
              private Integer taskPageSize = 12;
              @Builder.Default
              private RaptorConfig raptor = new RaptorConfig();
              @Builder.Default
              private List<String> entityTypes = Arrays.asList("organization", "person", "location", "event", "time");

              @Data
              @Builder
              @AllArgsConstructor
              @NoArgsConstructor
              public static class RaptorConfig {
                     @Builder.Default
                     private Boolean useRaptor = false;
              }
       }
}