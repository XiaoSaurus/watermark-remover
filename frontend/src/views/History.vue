<template>
  <div class="app">
    <header class="navbar">
      <div class="brand">
        <span class="brand-icon">✂️</span>
        <span class="brand-name">去水印工具</span>
      </div>
      <nav>
        <router-link to="/" class="nav-link">解析下载</router-link>
        <router-link to="/history" class="nav-link">历史记录</router-link>
      </nav>
    </header>
    <main class="main">
      <el-card class="card">
        <template #header><b>历史记录（本次会话）</b></template>
        <el-empty description="暂无记录，去解析一个视频吧～" />
      </el-card>
    </main>
  </div>
</template>

<script setup>
</script>

<style scoped>
.app { min-height: 100vh; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.navbar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 32px; height: 60px; background: rgba(255,255,255,0.1);
  backdrop-filter: blur(10px);
}
.brand { display: flex; align-items: center; gap: 8px; color: #fff; font-size: 20px; font-weight: bold; }
.nav-link { color: rgba(255,255,255,0.85); text-decoration: none; margin-left: 24px; font-size: 15px; }
.nav-link:hover, .nav-link.router-link-active { color: #fff; }
.main { max-width: 760px; margin: 0 auto; padding: 40px 16px; }
.card { border-radius: 16px; }
</style>