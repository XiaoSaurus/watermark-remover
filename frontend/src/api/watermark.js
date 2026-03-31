import axios from "axios";

const http = axios.create({ baseURL: "/api", timeout: 30000 });
http.interceptors.response.use(res => res.data, err => Promise.reject(err));

export const videoApi = {
  parse(url) { return http.post("/video/parse", { url }); },
};

// 解析历史
export const parseHistoryApi = {
  save(item)              { return http.post("/parse-history", item); },
  list(page=0, size=20)   { return http.get("/parse-history", { params: { page, size } }); },
  remove(id)              { return http.delete(`/parse-history/${id}`); },
  clear()                 { return http.delete("/parse-history"); },
};

// 下载历史
export const historyApi = {
  save(item)              { return http.post("/history", item); },
  list(page=0, size=20)   { return http.get("/history", { params: { page, size } }); },
  remove(id)              { return http.delete(`/history/${id}`); },
  clear()                 { return http.delete("/history"); },
};
