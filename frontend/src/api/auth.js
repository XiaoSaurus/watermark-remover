import axios from "axios";

// 直接使用全局 axios 实例，复用 main.js 中配置的拦截器（token注入、401跳转）
// 不再创建独立实例，确保所有请求都经过全局拦截器处理

const API_BASE = "/api";

export const authApi = {
  /**
   * 发送短信验证码
   * @param {string} phone 手机号
   * @param {string} scene 场景：login/register/reset
   */
  sendSms(phone, scene) {
    return axios.post(`${API_BASE}/auth/sms/send`, { phone, scene });
  },

  /**
   * 验证短信验证码
   * @param {string} phone 手机号
   * @param {string} code 验证码
   * @param {string} scene 场景
   */
  verifySmsCode(phone, code, scene) {
    return axios.post(`${API_BASE}/auth/sms/verify`, { phone, code, scene });
  },

  /**
   * 登录
   * @param {string} type 登录方式：phone/wechat_web/wechat_miniprogram/tourist
   * @param {object} data 登录数据
   */
  login(type, data) {
    return axios.post(`${API_BASE}/auth/login`, { type, ...data });
  },

  /**
   * 注册
   * @param {string} phone 手机号
   * @param {string} code 验证码
   * @param {string} password 密码
   * @param {string} confirmPassword 确认密码
   * @param {string} username 用户名（可选）
   */
  register(phone, code, password, confirmPassword, username) {
    const data = { type: "phone", phone, code, password, confirmPassword };
    if (username) data.username = username;
    return axios.post(`${API_BASE}/auth/register`, data);
  },

  /**
   * 重置密码
   * @param {string} phone 手机号
   * @param {string} code 验证码
   * @param {string} newPassword 新密码
   * @param {string} confirmPassword 确认密码
   */
  resetPassword(phone, code, newPassword, confirmPassword) {
    return axios.post(`${API_BASE}/auth/reset-password`, {
      phone,
      code,
      newPassword,
      confirmPassword
    });
  },

  /**
   * 获取当前用户信息
   */
  getMe() {
    return axios.get(`${API_BASE}/auth/me`);
  },

  /**
   * 检查用户名是否存在
   * @param {string} username 用户名
   */
  checkUsername(username) {
    return axios.get(`${API_BASE}/user/check-username`, { params: { username } });
  }
};

/**
 * 头像 API
 */
export const avatarApi = {
  /**
   * 获取随机头像
   */
  getRandomAvatar() {
    return axios.get(`${API_BASE}/avatar/random`);
  },

  /**
   * 获取头像列表
   * @param {string} category 分类
   */
  getAvatarList(category) {
    return axios.get(`${API_BASE}/avatar/list`, { params: { category } });
  },

  /**
   * 上传头像
   * @param {File} file 文件
   */
  uploadAvatar(file) {
    const formData = new FormData();
    formData.append("file", file);
    return axios.post(`${API_BASE}/avatar/upload`, formData, {
      headers: { "Content-Type": "multipart/form-data" }
    });
  }
};
