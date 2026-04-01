package com.watermark.service;

import com.watermark.model.dto.*;

public interface UserService {
    UserVO loginByPhone(LoginRequest req) throws Exception;
    UserVO loginByWechatMiniprogram(LoginRequest req) throws Exception;
    UserVO loginByWechatWeb(LoginRequest req) throws Exception;
    UserVO loginAsTourist() throws Exception;
    UserVO register(RegisterRequest req) throws Exception;
    void resetPassword(ResetPasswordRequest req) throws Exception;
    UserVO getUserInfo(Long userId);
    boolean checkUsernameExists(String username);
    
    /**
     * 更新用户头像
     */
    void updateAvatar(Long userId, String avatarUrl);
    
    /**
     * 从随机头像列表选择更新用户头像
     */
    void updateAvatarFromList(Long userId, Long avatarId);
    
    // ===== 新增用户资料相关方法 =====
    
    /**
     * 获取用户详细资料
     */
    UserProfileVO getUserProfile(Long userId);
    
    /**
     * 更新用户资料
     */
    void updateProfile(Long userId, UpdateProfileRequest req) throws Exception;
    
    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordRequest req) throws Exception;
    
    /**
     * 绑定手机号
     */
    void bindPhone(Long userId, BindPhoneRequest req) throws Exception;
    
    /**
     * 增加积分
     */
    void addPoints(Long userId, int points);
    
    /**
     * 根据积分计算等级
     */
    int calculateLevel(int points);
}
