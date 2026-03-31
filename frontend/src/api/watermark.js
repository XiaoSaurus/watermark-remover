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
  listTasks() {
    return Promise.resolve({ code: 200, data: [] });
  },
  downloadUrl(taskId) {
    return `/api/video/download/${taskId}`;
  },
};