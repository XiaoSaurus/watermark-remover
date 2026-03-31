import { createRouter, createWebHistory } from "vue-router";
import Home from "@/views/Home.vue";
import History from "@/views/History.vue";

const routes = [
  { path: "/", component: Home, meta: { title: "去水印工具" } },
  { path: "/history", component: History, meta: { title: "历史记录" } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  document.title = to.meta.title || "去水印工具";
});

export default router;