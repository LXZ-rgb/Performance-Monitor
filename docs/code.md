---
layout: page
title: 项目源码与结构介绍（博客导航页）
---

# 🌟 项目所有 Java 源码、FXML 文件展示与模块导航

<div align="center" style="margin-bottom: 1em;">
  <img src="https://img.shields.io/badge/Java-PerformanceMonitor-blue?logo=java" alt="Java" style="max-width:120px;">
  <img src="https://img.shields.io/badge/技术栈-JavaFX%20%7C%20OSHI%20%7C%20SQLite-green" alt="Stack" style="max-width:200px;">
  <img src="https://img.shields.io/badge/开源-GitHub-brightgreen" alt="GitHub" style="max-width:120px;">
</div>

> 本页面为 Performance Monitor 答辩专用源码/界面展示博客主页，可跳转至各个模块源码或FXML文件详细页面，含注释与结构说明。适合学习、答辩和代码审阅。

---

## 🗂️ 模块导航

| 模块 | 说明 | 跳转 |
|:----|:-----|:-----|
| logic 包 | 业务逻辑、数据工具 | [logic 包源码与解读](code_logic.md) |
| ui 包 | 主程序与控制器 | [ui 包源码与解读](code_ui.md) |
| FXML 文件 | 界面布局文件 | [FXML 文件展示与说明](code_fxml.md) |

---

## 📝 说明

- 所有源码和 FXML 布局文件均带详细中文注释，方便理解。
- 每个模块均有目录、结构图及功能简述，可点击上表跳转详细页面。
- 如需查看具体文件，进入模块页后可定位至相应段落。

---

## 📦 目录结构概览

```text
performance-monitor/
├── performance-monitor/
│   └── src/
│       └── main/
│           ├── java/
│           │   ├── logic/        # 逻辑层代码
│           │   └── ui/           # 界面层与控制器
│           └── resources/
│               └── ui/           # FXML布局文件
│               └── img/          # 图片资源
```

---

<div align="center" style="margin-top:2em;">
  <a href="code_logic.md"><b>进入 logic 包源码与解读</b></a> |
  <a href="code_ui.md"><b>进入 ui 包源码与解读</b></a> |
  <a href="code_fxml.md"><b>查看 FXML 文件展示</b></a>
</div>

---

