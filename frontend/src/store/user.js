import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { authApi } from "@/api/auth";

export const useUserStore = defineStore("user", () => {
  const user = ref(null);
  const token = ref(localStorage.getItem("token") || "");

  const isLoggedIn = computed(() => !!token.value && !!user.value);

  async function login(type, data) {
    const res = await authApi.login(type, data);
    if (res.code === 200) {
      token.value = res.data.token;
      user.value = res.data;
      localStorage.setItem("token", token.value);
      return res.data;
    }
    throw new Error(res.message);
  }

  async function register(phone, code, password, confirmPassword, username) {
    const res = await authApi.register(phone, code, password, confirmPassword, username);
    if (res.code === 200) {
      token.value = res.data.token;
      user.value = res.data;
      localStorage.setItem("token", token.value);
      return res.data;
    }
    throw new Error(res.message);
  }

  async function resetPassword(phone, code, newPassword, confirmPassword) {
    const res = await authApi.resetPassword(phone, code, newPassword, confirmPassword);
    if (res.code === 200) return res.data;
    throw new Error(res.message);
  }

  function logout() {
    token.value = "";
    user.value = null;
    localStorage.removeItem("token");
  }

  // 微信扫码登录后设置token和用户信息
  function setToken(newToken) {
    token.value = newToken;
    localStorage.setItem("token", newToken);
  }

  function setUser(userData) {
    user.value = userData;
  }

  return {
    user,
    token,
    isLoggedIn,
    login,
    register,
    resetPassword,
    logout,
    setToken,
    setUser
  };
});
