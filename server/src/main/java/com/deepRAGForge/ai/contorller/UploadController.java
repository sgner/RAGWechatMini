package com.deepRAGForge.ai.contorller;
import com.deepRAGForge.ai.chat.deepseek.response.DocumentUploadResponse;
import com.deepRAGForge.ai.dto.FileInitRequest;
import com.deepRAGForge.ai.dto.UploadFileRequest;
import com.deepRAGForge.ai.entity.ChunkInfo;
import com.deepRAGForge.ai.enums.ErrorCode;
import com.deepRAGForge.ai.result.R;
import com.deepRAGForge.ai.service.FileUploadService;
import com.deepRAGForge.ai.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(methods = {RequestMethod.DELETE,RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT},allowCredentials = "true",originPatterns = "*")
public class UploadController {
     private final FileUploadService fileUploadService;
     @PostMapping("/files")
     public R uploadFile(@RequestParam String fileId, @RequestParam int chunkIndex, @RequestParam String kbName, @RequestParam String kbId,@RequestParam MultipartFile chunk){
          try{
              DocumentUploadResponse documentUploadResponse = fileUploadService.uploadChunk(fileId, chunkIndex, chunk, kbName, kbId);
              log.info("文件上传成功");
              if (documentUploadResponse.getCode()==101){
                  log.info(documentUploadResponse.getMessage());
                  return R.error(ErrorCode.SYSTEM_ERROR,documentUploadResponse.getMessage());
              }
              return R.success();
          }catch(Exception e){
               e.printStackTrace();
               return R.error(ErrorCode.SYSTEM_ERROR,e.getMessage());
          }
     }
     @GetMapping("/get/chunks")
     public R getChunks(String fileId){
         List<ChunkInfo> chunks = fileUploadService.getChunks(fileId);
         log.info(chunks.toString());
         return R.success(chunks);
     }
     @PostMapping("/init")
    public R initUpload(@RequestBody FileInitRequest request){
         try{
             log.info("进行初始化");
             log.info(ThreadLocalUtil.get());
             if(ThreadLocalUtil.get() == null){
                 return R.error(ErrorCode.SYSTEM_ERROR,"");
             }
             fileUploadService.initUpload(request);
             log.info("成功进行了初始化： "+request.toString());
         }catch (Exception e){
             e.printStackTrace();
           return R.error(ErrorCode.SYSTEM_ERROR,e.getMessage());
     }
         return R.success();
     }
}
