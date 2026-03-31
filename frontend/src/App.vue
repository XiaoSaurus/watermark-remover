<template>
  <el-config-provider>
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { ElConfigProvider } from "element-plus";
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: "PingFang SC", "Microsoft YaHei", sans-serif; background: #f5f7fa; }
</style>