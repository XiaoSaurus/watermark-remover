import axios from "axios";

// 直接使用全局 axios 实例，复用 main.js 中配置的拦截器（token注入、401跳转）
// 不再创建独立实例，确保所有请求都经过全局拦截器处理

const API_BASE = "/api";

export const videoApi = {
  parse(url) { return axios.post(`${API_BASE}/video/parse`, { url }); },
};

// 解析历史
export const parseHistoryApi = {
  save(item)              { return axios.post(`${API_BASE}/parse-history`, item); },
  list(page=0, size=20)   { return axios.get(`${API_BASE}/parse-history`, { params: { page, size } }); },
  remove(id)              { return axios.delete(`${API_BASE}/parse-history/${id}`); },
  clear()                 { return axios.delete(`${API_BASE}/parse-history`); },
};

// 下载历史
export const historyApi = {
  save(item)              { return axios.post(`${API_BASE}/history`, item); },
  list(page=0, size=20)   { return axios.get(`${API_BASE}/history`, { params: { page, size } }); },
  remove(id)              { return axios.delete(`${API_BASE}/history/${id}`); },
  clear()                 { return axios.delete(`${API_BASE}/history`); },
};
