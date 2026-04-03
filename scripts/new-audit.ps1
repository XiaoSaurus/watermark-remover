<#
.SYNOPSIS
  一键生成新的项目审计报告（PROJECT_OVERVIEW.md）到版本化目录。

.DESCRIPTION
  在 docs/project-audit/versions/<版本目录>/ 下创建 PROJECT_OVERVIEW.md，
  由 docs/project-audit/TEMPLATE_PROJECT_OVERVIEW.md 填充日期与路径占位符。

.PARAMETER VersionId
  版本目录名，如 2026-04-15 或 2026-04-15-1430。默认：今天日期 yyyy-MM-dd。

.PARAMETER ForceTimeSuffix
  强制使用「日期-小时分钟」作为目录名（避免同一天多次生成冲突）。

.PARAMETER Force
  若目标 PROJECT_OVERVIEW.md 已存在，覆盖写入。

.EXAMPLE
  .\scripts\new-audit.ps1

.EXAMPLE
  .\scripts\new-audit.ps1 -VersionId 2026-05-01

.EXAMPLE
  .\scripts\new-audit.ps1 -ForceTimeSuffix
#>

[CmdletBinding()]
param(
    [string] $VersionId = "",
    [switch] $ForceTimeSuffix,
    [switch] $Force
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$auditRoot = Join-Path $repoRoot "docs\project-audit"
$templatePath = Join-Path $auditRoot "TEMPLATE_PROJECT_OVERVIEW.md"
$versionsRoot = Join-Path $auditRoot "versions"

if (-not (Test-Path -LiteralPath $templatePath)) {
    Write-Error "缺少模板文件: $templatePath"
}

if (-not (Test-Path -LiteralPath $versionsRoot)) {
    New-Item -ItemType Directory -Path $versionsRoot | Out-Null
}

$now = Get-Date
$generatedAt = $now.ToString("yyyy-MM-dd HH:mm")

function Get-DefaultVersionId {
    if ($ForceTimeSuffix) {
        return $now.ToString("yyyy-MM-dd-HHmm")
    }
    return $now.ToString("yyyy-MM-dd")
}

if ([string]::IsNullOrWhiteSpace($VersionId)) {
    $VersionId = Get-DefaultVersionId
}

# 若目录已存在且未 -Force，且未指定 ForceTimeSuffix，则自动加时间后缀避免覆盖
$targetDir = Join-Path $versionsRoot $VersionId
$targetFile = Join-Path $targetDir "PROJECT_OVERVIEW.md"

if ((Test-Path -LiteralPath $targetFile) -and -not $Force -and -not $ForceTimeSuffix) {
    $VersionId = $now.ToString("yyyy-MM-dd-HHmm")
    $targetDir = Join-Path $versionsRoot $VersionId
    $targetFile = Join-Path $targetDir "PROJECT_OVERVIEW.md"
    Write-Host "目标已存在，已自动改用版本目录: $VersionId" -ForegroundColor Yellow
}

if ((Test-Path -LiteralPath $targetFile) -and -not $Force) {
    Write-Error "文件已存在: $targetFile （使用 -Force 覆盖，或 -ForceTimeSuffix 指定新目录）"
}

if (-not (Test-Path -LiteralPath $targetDir)) {
    New-Item -ItemType Directory -Path $targetDir | Out-Null
}

$relReportPath = "docs/project-audit/versions/$VersionId/PROJECT_OVERVIEW.md"
$content = Get-Content -LiteralPath $templatePath -Raw -Encoding UTF8
$content = $content.Replace("{{GENERATED_AT}}", $generatedAt).Replace("{{VERSION_ID}}", "v$VersionId").Replace("{{REPORT_PATH}}", $relReportPath)

Set-Content -LiteralPath $targetFile -Value $content -Encoding UTF8

Write-Host "已生成: $targetFile" -ForegroundColor Green
Write-Host "相对路径: $relReportPath"
