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
        <el-row :gutter="24">
          <!-- 左侧：上传区域 -->
          <el-col :span="14">
            <el-card class="card">
              <template #header><b>上传文件</b></template>

              <el-upload
                class="upload-area"
                drag
                :auto-upload="false"
                :on-change="handleFileChange"
                :show-file-list="false"
                accept="image/*,video/*"
              >
                <el-icon size="48" color="#409eff"><UploadFilled /></el-icon>
                <div class="upload-text">拖拽文件到此处，或 <em>点击上传</em></div>
                <div class="upload-hint">支持 JPG、PNG、MP4、MOV 等格式，最大 100MB</div>
              </el-upload>

              <!-- 预览 -->
              <div v-if="previewUrl" class="preview-wrap">
                <img v-if="fileType === 'image'" :src="previewUrl" class="preview-img" />
                <video v-else :src="previewUrl" class="preview-img" controls />
                <div class="file-info">
                  <el-tag>{{ selectedFile?.name }}</el-tag>
                  <el-tag type="success">{{ fileType === 'image' ? '图片' : '视频' }}</el-tag>
                </div>
              </div>
            </el-card>
          </el-col>

          <!-- 右侧：参数设置 -->
          <el-col :span="10">
            <el-card class="card">
              <template #header><b>水印区域设置</b></template>

              <el-form :model="form" label-width="80px">
                <el-form-item label="X 坐标">
                  <el-input-number v-model="form.x" :min="0" style="width:100%" />
                </el-form-item>
                <el-form-item label="Y 坐标">
                  <el-input-number v-model="form.y" :min="0" style="width:100%" />
                </el-form-item>
                <el-form-item label="宽度">
                  <el-input-number v-model="form.width" :min="1" style="width:100%" />
                </el-form-item>
                <el-form-item label="高度">
                  <el-input-number v-model="form.height" :min="1" style="width:100%" />
                </el-form-item>
              </el-form>

              <el-alert type="info" :closable="false" style="margin-bottom:16px">
                <template #title>
                  提示：坐标从左上角(0,0)开始，单位为像素
                </template>
              </el-alert>

              <el-button
                type="primary"
                size="large"
                :loading="submitting"
                :disabled="!selectedFile"
                @click="handleSubmit"
                style="width:100%"
              >
                {{ submitting ? '处理中...' : '开始去水印' }}
              </el-button>
            </el-card>

            <!-- 任务状态 -->
            <el-card v-if="currentTask" class="card" style="margin-top:16px">
              <template #header><b>处理状态</b></template>
              <div class="task-status">
                <el-steps :active="stepIndex" finish-status="success">
                  <el-step title="上传" />
                  <el-step title="处理中" />
                  <el-step title="完成" />
                </el-steps>

                <div class="status-info">
                  <el-tag :type="statusType">{{ statusText }}</el-tag>
                  <span v-if="currentTask.errorMsg" class="error-msg">{{ currentTask.errorMsg }}</span>
                </div>

                <el-button
                  v-if="currentTask.status === 'done'"
                  type="success"
                  @click="handleDownload"
                  style="width:100%;margin-top:12px"
                >
                  <el-icon><Download /></el-icon> 下载结果
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { ElMessage } from "element-plus";
import { watermarkApi } from "@/api/watermark";

const selectedFile = ref(null);
const previewUrl = ref("");
const fileType = ref("image");
const submitting = ref(false);
const currentTask = ref(null);
let pollTimer = null;

const form = ref({ x: 0, y: 0, width: 100, height: 50 });

const stepIndex = computed(() => {
  if (!currentTask.value) return 0;
  const s = currentTask.value.status;
  if (s === "pending") return 1;
  if (s === "processing") return 1;
  if (s === "done") return 3;
  return 1;
});

const statusType = computed(() => {
  const map = { pending: "info", processing: "warning", done: "success", failed: "danger" };
  return map[currentTask.value?.status] || "info";
});

const statusText = computed(() => {
  const map = { pending: "等待处理", processing: "处理中...", done: "处理完成", failed: "处理失败" };
  return map[currentTask.value?.status] || "";
});

function handleFileChange(file) {
  selectedFile.value = file.raw;
  previewUrl.value = URL.createObjectURL(file.raw);
  fileType.value = file.raw.type.startsWith("video") ? "video" : "image";
}

async function handleSubmit() {
  if (!selectedFile.value) return;
  submitting.value = true;
  currentTask.value = null;

  try {
    const fd = new FormData();
    fd.append("file", selectedFile.value);
    fd.append("type", fileType.value);
    fd.append("x", form.value.x);
    fd.append("y", form.value.y);
    fd.append("width", form.value.width);
    fd.append("height", form.value.height);

    const res = await watermarkApi.submit(fd);
    if (res.code === 200) {
      currentTask.value = res.data;
      ElMessage.success("任务已提交，正在处理...");
      startPolling(res.data.id);
    } else {
      ElMessage.error(res.message);
    }
  } catch (e) {
    ElMessage.error("提交失败，请检查后端服务");
  } finally {
    submitting.value = false;
  }
}

function startPolling(taskId) {
  clearInterval(pollTimer);
  pollTimer = setInterval(async () => {
    const res = await watermarkApi.getTask(taskId);
    if (res.code === 200) {
      currentTask.value = res.data;
      if (["done", "failed"].includes(res.data.status)) {
        clearInterval(pollTimer);
        if (res.data.status === "done") ElMessage.success("处理完成！");
        else ElMessage.error("处理失败：" + res.data.errorMsg);
      }
    }
  }, 2000);
}

function handleDownload() {
  window.open(watermarkApi.downloadUrl(currentTask.value.id));
}
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
.upload-area { width: 100%; }
.upload-text { font-size: 16px; margin-top: 12px; }
.upload-hint { color: #999; font-size: 13px; margin-top: 4px; }
.preview-wrap { margin-top: 16px; text-align: center; }
.preview-img { max-width: 100%; max-height: 300px; border-radius: 8px; border: 1px solid #eee; }
.file-info { margin-top: 8px; display: flex; gap: 8px; justify-content: center; }
.task-status { padding: 8px 0; }
.status-info { margin-top: 16px; display: flex; align-items: center; gap: 8px; }
.error-msg { color: #f56c6c; font-size: 13px; }
</style>