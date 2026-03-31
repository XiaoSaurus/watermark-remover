<template>
  <div class="page">
    <el-container>
      <el-header class="header">
        <div class="logo">
          <el-icon size="28"><MagicStick /></el-icon>
          <span>去水印工具</span>
        </div>
        <el-menu mode="horizontal" :ellipsis="false" router>
          <el-menu-item index="/">上传处理</el-menu-item>
          <el-menu-item index="/history">历史记录</el-menu-item>
        </el-menu>
      </el-header>

      <el-main class="main">
        <el-card class="card">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <b>历史记录</b>
              <el-button @click="loadTasks" :loading="loading">
                <el-icon><Refresh /></el-icon> 刷新
              </el-button>
            </div>
          </template>

          <el-table :data="tasks" v-loading="loading" empty-text="暂无记录">
            <el-table-column prop="id" label="任务ID" width="280">
              <template #default="{ row }">
                <el-text truncated>{{ row.id }}</el-text>
              </template>
            </el-table-column>
            <el-table-column prop="originalFileName" label="文件名" />
            <el-table-column prop="fileType" label="类型" width="80">
              <template #default="{ row }">
                <el-tag size="small">{{ row.fileType === 'image' ? '图片' : '视频' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="创建时间" width="180" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'done'"
                  type="primary" size="small" link
                  @click="download(row.id)"
                >
                  下载
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { watermarkApi } from "@/api/watermark";

const tasks = ref([]);
const loading = ref(false);

async function loadTasks() {
  loading.value = true;
  try {
    const res = await watermarkApi.listTasks();
    if (res.code === 200) tasks.value = res.data;
  } finally {
    loading.value = false;
  }
}

function statusType(s) {
  return { pending: "info", processing: "warning", done: "success", failed: "danger" }[s] || "info";
}
function statusText(s) {
  return { pending: "等待", processing: "处理中", done: "完成", failed: "失败" }[s] || s;
}
function download(id) {
  window.open(watermarkApi.downloadUrl(id));
}

onMounted(loadTasks);
</script>

<style scoped>
.page { min-height: 100vh; }
.header {
  display: flex; align-items: center; justify-content: space-between;
  background: #fff; border-bottom: 1px solid #eee; padding: 0 24px;
}
.logo { display: flex; align-items: center; gap: 8px; font-size: 20px; font-weight: bold; color: #409eff; }
.main { padding: 24px; }
.card { border-radius: 12px; }
</style>