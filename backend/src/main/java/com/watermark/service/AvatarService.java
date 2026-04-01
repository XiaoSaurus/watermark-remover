package com.watermark.service;

import com.watermark.model.Avatar;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AvatarService {

    /**
     * 随机获取一个头像
     */
    Avatar getRandomAvatar();

    /**
     * 按分类随机获取一个头像
     */
    Avatar getRandomAvatarByCategory(String category);

    /**
     * 按分类获取头像列表
     */
    List<Avatar> getAvatarListByCategory(String category);

    /**
     * 获取所有头像
     */
    List<Avatar> getAllAvatars();

    /**
     * 上传用户头像，返回保存后的 URL 路径
     */
    String uploadAvatar(MultipartFile file, Long userId) throws Exception;
}
