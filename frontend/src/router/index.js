import { createRouter, createWebHistory } from "vue-router";
import Home from "@/views/Home.vue";
import ParseHistory from "@/views/ParseHistory.vue";
import History from "@/views/History.vue";

const routes = [
  { path: "/", component: Home, meta: { title: "去水印工具" } },
  { path: "/parse-history", component: ParseHistory, meta: { title: "解析历史" } },
  { path: "/history", component: History, meta: { title: "下载历史" } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  document.title = to.meta.title || "去水印工具";
});

export default router;
