package com.pcadvisor.controller;

import com.pcadvisor.common.Result;
import com.pcadvisor.common.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    // 相对于 static 目录的路径，最终映射为 /images/evaluations/**
    private static final String UPLOAD_SUB_DIR = "images/evaluations";

    @Value("${app.upload.base-dir:./src/main/resources/static}")
    private String uploadBaseDir;

    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileUploadException("文件为空");
        }

        // 校验类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileUploadException("只允许上传图片文件");
        }

        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString() + ext;

        // 目标目录
        Path uploadDir = Paths.get(uploadBaseDir, UPLOAD_SUB_DIR).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new FileUploadException("创建上传目录失败: " + e.getMessage());
        }

        // 写入文件
        Path destPath = uploadDir.resolve(newFileName);
        try {
            Files.copy(file.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileUploadException("文件保存失败: " + e.getMessage());
        }

        // 返回访问URL
        String url = "/" + UPLOAD_SUB_DIR + "/" + newFileName;

        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        result.put("originalName", originalName);
        return Result.success("上传成功", result);
    }

    /**
     * 批量上传
     */
    @PostMapping("/upload/batch")
    public Result<List<Map<String, Object>>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        List<Map<String, Object>> results = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            Result<Map<String, Object>> single = uploadImage(file);
            if (single.getData() != null) {
                results.add(single.getData());
            }
        }
        return Result.success(results);
    }
}
