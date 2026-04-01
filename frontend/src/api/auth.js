import axios from "axios";

const http = axios.create({
  baseURL: "/api",
  timeout: 30000
});

http.interceptors.response.use(
  res => res.data,
  err => {
    // 统一处理网络错误
    const msg = err.response?.data?.message || err.message || "网络请求失败";
    return Promise.reject(new Error(msg));
  }
);

export const authApi = {
  /**
   * 发送短信验证码
   * @param {string} phone 手机号
   * @param {string} scene 场景：login/register/reset
   */
  sendSms(phone, scene) {
    return http.post("/auth/sms/send", { phone, scene });
  },

  /**
   * 验证短信验证码
   * @param {string} phone 手机号
   * @param {string} code 验证码
   * @param {string} scene 场景
   */
  verifySmsCode(phone, code, scene) {
    return http.post("/auth/sms/verify", { phone, code, scene });
  },

  /**
   * 登录
   * @param {string} type 登录方式：phone/wechat_web/wechat_miniprogram/tourist
   * @param {object} data 登录数据
   */
  login(type, data) {
    return http.post("/auth/login", { type, ...data });
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
    return http.post("/auth/register", data);
  },

  /**
   * 重置密码
   * @param {string} phone 手机号
   * @param {string} code 验证码
   * @param {string} newPassword 新密码
   * @param {string} confirmPassword 确认密码
   */
  resetPassword(phone, code, newPassword, confirmPassword) {
    return http.post("/auth/reset-password", {
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
    return http.get("/auth/me");
  },

  /**
   * 检查用户名是否存在
   * @param {string} username 用户名
   */
  checkUsername(username) {
    return http.get("/user/check-username", { params: { username } });
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
    return http.get("/avatar/random");
  },

  /**
   * 获取头像列表
   * @param {string} category 分类
   */
  getAvatarList(category) {
    return http.get("/avatar/list", { params: { category } });
  },

  /**
   * 上传头像
   * @param {File} file 文件
   */
  uploadAvatar(file) {
    const formData = new FormData();
    formData.append("file", file);
    return http.post("/avatar/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" }
    });
  }
};
