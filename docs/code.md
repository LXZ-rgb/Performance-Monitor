---
layout: page
title: 项目源码与结构介绍
---

## 项目仓库

源码地址：[https://github.com/LXZ-rgb/Performance-Monitor](https://github.com/LXZ-rgb/Performance-Monitor)

---

## 主要功能

- 性能监控与数据采集
- 数据库存储与管理
- Excel 报表导出
- 团队博客与文章发布

---

## 源代码结构

```
Performance_monitor/
├── src/
│   └── main/
│       └── java/
│           ├── logic/
│           │   ├── DatabaseHandler.java      // 数据库操作
│           │   ├── ExcelExporter.java        // Excel 导出
│           │   └── PerformanceData.java      // 性能数据模型
│           └── ui/
│               ├── BrandLogoManager.java     // Logo 管理
│               ├── MainApp.java              // 应用入口
│               └── MainController.java       // 主界面控制
├── icons/                                     // 应用图标
└── ...
```

---

## 核心模块简介

- **logic/**  
  - `DatabaseHandler.java`：负责数据的持久化操作  
  - `ExcelExporter.java`：实现性能数据的导出功能  
  - `PerformanceData.java`：定义性能数据结构和相关操作  

- **ui/**  
  - `MainApp.java`：应用程序主入口  
  - `MainController.java`：主界面事件与逻辑控制  
  - `BrandLogoManager.java`：品牌 Logo 加载与管理  

---

## 技术栈

- Java 8+
- JavaFX (界面实现)
- SQLite（或其他数据库，具体见源码）
- Jekyll + Minima（团队博客前端）

---

> 欢迎 Star、Fork 本项目，和我们一起成长！

---

[返回首页](index.md)
