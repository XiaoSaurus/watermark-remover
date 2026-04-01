import { createRouter, createWebHistory } from "vue-router";
import Home from "@/views/Home.vue";
import ParseHistory from "@/views/ParseHistory.vue";
import History from "@/views/History.vue";
import Login from "@/views/Login.vue";
import Profile from "@/views/Profile.vue";
import { useUserStore } from "@/store/user";

const routes = [
  { path: "/", component: Home, meta: { title: "去水印工具" } },
  { path: "/parse-history", component: ParseHistory, meta: { title: "解析历史" } },
  { path: "/history", component: History, meta: { title: "下载历史" } },
  { path: "/login", component: Login, meta: { title: "登录" } },
  { path: "/profile", component: Profile, meta: { title: "个人资料", requiresAuth: true } },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || "去水印工具";

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    const userStore = useUserStore();
    if (!userStore.isLoggedIn) {
      next("/login");
    } else {
      next();
    }
  } else {
    next();
  }
});

export default router;
