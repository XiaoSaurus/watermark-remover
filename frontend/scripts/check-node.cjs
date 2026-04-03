/**
 * 在 dev/build 前检查 Node 版本（Vite 5 需要 >=18）
 */
var major = parseInt(process.versions.node.split(".")[0], 10);
if (major < 18) {
  console.error("");
  console.error("[watermark-remover-frontend] 当前 Node: " + process.version);
  console.error("[watermark-remover-frontend] 需要 Node.js 18 或更高版本（推荐 20 LTS）。");
  console.error("下载: https://nodejs.org/zh-cn/");
  console.error("");
  process.exit(1);
}
