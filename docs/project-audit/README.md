# 项目审计文档（版本化）

该目录用于沉淀“全项目扫描”结果，按日期版本保存，便于追踪技术栈、架构和方案变化。

## 目录约定

- `versions/YYYY-MM-DD/PROJECT_OVERVIEW.md`：当天扫描的完整项目报告

## 使用建议

- 每次你需要“重新全盘梳理项目”时，新增一个日期目录并生成新报告
- 若同一天多次生成，可使用 `YYYY-MM-DD-HHMM` 作为目录名
- 对比版本变化时，直接对比两个 `PROJECT_OVERVIEW.md`

## 一键生成新版本报告（PowerShell）

在仓库根目录执行：

```powershell
.\scripts\new-audit.ps1
```

常用参数：

| 参数 | 说明 |
|------|------|
| `-VersionId 2026-04-15` | 指定版本目录名（默认当天 `yyyy-MM-dd`） |
| `-ForceTimeSuffix` | 目录名强制为 `yyyy-MM-dd-HHmm` |
| `-Force` | 覆盖已存在的 `PROJECT_OVERVIEW.md` |

模板文件：`docs/project-audit/TEMPLATE_PROJECT_OVERVIEW.md`（可按需修改结构）。
