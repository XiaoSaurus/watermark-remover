<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>
<script setup>
import { ElConfigProvider } from "element-plus";
import zhCn from "element-plus/es/locale/lang/zh-cn";
import { onMounted } from "vue";
onMounted(() => {
  const saved = localStorage.getItem("wm_theme") || "light";
  document.documentElement.setAttribute("data-theme", saved);
});
</script>
<style>
@import "./assets/styles/theme.css";
@import "./assets/styles/base.css";
</style>
