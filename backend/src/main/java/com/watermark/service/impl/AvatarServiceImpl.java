package com.watermark.service.impl;

import com.watermark.model.Avatar;
import com.watermark.repository.AvatarRepository;
import com.watermark.service.AvatarService;
import com.watermark.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarServiceImpl implements AvatarService {

    private final AvatarRepository avatarRepository;
    private final SnowflakeIdGenerator snowflake;

    @Value("${app.upload.avatar-path:uploads/avatars}")
    private String avatarUploadPath;

    private static final String DEFAULT_AVATAR = "https://api.dicebear.com/7.x/bottts/png?seed=default";

    @Override
    public Avatar getRandomAvatar() {
        return avatarRepository.findRandomAvatar()
                .orElseGet(() -> Avatar.builder()
                        .id(snowflake.nextId())
                        .url(DEFAULT_AVATAR)
                        .category("momo")
                        .build());
    }

    @Override
    public Avatar getRandomAvatarByCategory(String category) {
        return avatarRepository.findRandomAvatarByCategory(category)
                .orElseGet(() -> getRandomAvatar());
    }

    @Override
    public List<Avatar> getAvatarListByCategory(String category) {
        if (category == null || category.isEmpty()) {
            return avatarRepository.findAll();
        }
        return avatarRepository.findByCategory(category);
    }

    @Override
    public List<Avatar> getAllAvatars() {
        return avatarRepository.findAll();
    }

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new Exception("上传文件不能为空");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new Exception("只支持上传图片文件");
        }

        // 检查文件大小 (最大 2MB)
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new Exception("图片大小不能超过 2MB");
        }

        // 生成文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            extension = ".png";
        }
        String filename = userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;

        // 确保目录存在
        Path uploadDir = Paths.get(avatarUploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 保存文件
        Path filePath = uploadDir.resolve(filename);
        file.transferTo(filePath.toFile());

        log.info("用户 {} 上传头像成功: {}", userId, filename);

        // 返回相对 URL 路径
        return "/api/avatar/file/" + filename;
    }
}
