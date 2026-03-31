import axios from "axios";

const http = axios.create({
  baseURL: "/api",
  timeout: 30000,
});

http.interceptors.response.use(
  (res) => res.data,
  (err) => Promise.reject(err)
);

export const videoApi = {
  parse(url) {
    return http.post("/video/parse", { url });
  },
};

export const historyApi = {
  // 保存一条记录到数据库
  save(item) {
    return http.post("/history", item);
  },
  // 获取历史列表（分页）
  list(page = 0, size = 20) {
    return http.get("/history", { params: { page, size } });
  },
  // 删除单条
  remove(id) {
    return http.delete(`/history/${id}`);
  },
  // 清空
  clear() {
    return http.delete("/history");
  },
};
