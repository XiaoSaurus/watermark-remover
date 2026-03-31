import axios from "axios";

const http = axios.create({
  baseURL: "/api",
  timeout: 120000,
});

http.interceptors.response.use(
  (res) => res.data,
  (err) => {
    console.error("请求失败:", err);
    return Promise.reject(err);
  }
);

export const watermarkApi = {
  // 提交去水印任务
  submit(formData) {
    return http.post("/watermark/submit", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },
  // 查询任务状态
  getTask(taskId) {
    return http.get(`/watermark/task/${taskId}`);
  },
  // 获取所有任务
  listTasks() {
    return http.get("/watermark/tasks");
  },
  // 下载结果
  downloadUrl(taskId) {
    return `/api/watermark/download/${taskId}`;
  },
};