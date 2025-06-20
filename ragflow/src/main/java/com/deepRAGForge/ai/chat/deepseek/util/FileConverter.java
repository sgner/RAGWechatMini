package com.deepRAGForge.ai.chat.deepseek.util;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Slf4j
public class FileConverter {
    public static List<File> convertMultipartFilesToFiles(List<MultipartFile> multipartFiles) throws IOException {
        if (multipartFiles == null || multipartFiles.isEmpty()) {
            return new ArrayList<>();
        }

        List<File> files = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {
            if (multipartFile != null && !multipartFile.isEmpty()) {
                // 使用UUID生成唯一文件名
                String uniqueFileName = multipartFile.getOriginalFilename();
                File file = new File(System.getProperty("java.io.tmpdir") +
                        File.separator +
                        uniqueFileName);

                multipartFile.transferTo(file);
                files.add(file);
            }
        }

        return files;
    }
    public static int deleteFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            return 0;
        }

        int deletedCount = 0;
        for (File file : files) {
            if (file != null && file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    deletedCount++;
                } else {
                    log.error("Failed to delete file: " + file.getAbsolutePath());
                }
            }
        }
        return deletedCount;
    }
}
