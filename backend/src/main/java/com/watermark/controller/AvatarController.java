package com.watermark.controller;

import com.watermark.model.Avatar;
import com.watermark.model.Result;
import com.watermark.service.AvatarService;
import com.watermark.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "头像管理")
@RestController
@RequestMapping("/api/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;
    private final UserService userService;

    @Value("${app.upload.avatar-path:uploads/avatars}")
    private String avatarUploadPath;

    @Operation(summary = "随机获取一个头像")
    @GetMapping("/random")
    public Result<Map<String, Object>> getRandomAvatar() {
        Avatar avatar = avatarService.getRandomAvatar();
        return Result.success(toMap(avatar));
    }

    @Operation(summary = "按分类随机获取一个头像")
    @GetMapping("/random/{category}")
    public Result<Map<String, Object>> getRandomAvatarByCategory(@PathVariable String category) {
        Avatar avatar = avatarService.getRandomAvatarByCategory(category);
        return Result.success(toMap(avatar));
    }

    @Operation(summary = "获取头像列表")
    @GetMapping("/list")
    public Result<List<Avatar>> getAvatarList(
            @RequestParam(required = false) String category) {
        List<Avatar> avatars;
        if (category != null && !category.isEmpty()) {
            avatars = avatarService.getAvatarListByCategory(category);
        } else {
            avatars = avatarService.getAllAvatars();
        }
        return Result.success(avatars);
    }

    @Operation(summary = "上传头像")
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        if (auth == null) {
            return Result.error("请先登录");
        }
        Long userId = (Long) auth.getPrincipal();
        try {
            String avatarUrl = avatarService.uploadAvatar(file, userId);
            // 更新用户头像
            userService.updateAvatar(userId, avatarUrl);

            Map<String, Object> result = new HashMap<>();
            result.put("url", avatarUrl);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取头像文件")
    @GetMapping("/file/{filename}")
    public ResponseEntity<Resource> getAvatarFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(avatarUploadPath).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                String contentType = "image/png";
                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (filename.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (filename.endsWith(".webp")) {
                    contentType = "image/webp";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private Map<String, Object> toMap(Avatar avatar) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", avatar.getId());
        map.put("url", avatar.getUrl());
        map.put("category", avatar.getCategory());
        return map;
    }
}
