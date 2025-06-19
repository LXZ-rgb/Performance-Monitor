---
layout: page
title: logic 包源码与结构详解
---

# 🧩 logic 包 — 数据、工具与业务逻辑

> 本页完整展示 `logic` 包下所有 Java 源码（含中文注释和模块说明），并按功能分组解读。

---

## 目录

- [DatabaseHandler.java](#databasehandlerjava-数据库操作)
- [HardwareMonitor.java](#hardwaremonitorjava-硬件监控)
- [ExcelExporter.java](#excelexporterjava-excel导出)
- [PerformanceData.java](#performancedatajava-性能数据模型)
- [ConfigManager.java](#configmanagerjava-配置管理)
- [LogHelper.java](#loghelperjava-日志工具)
- [CpuInfoParser.java](#cpuinfoparserjava-cpu型号解析)
- [FileUtils.java](#fileutilsjava-文件工具)

---

## DatabaseHandler.java （数据库操作）

> **作用**：管理 SQLite 数据库连接、建表、数据存储与关闭。所有性能异常数据均由此管理持久化。

```java
// ...（完整源码同主 code.md 文件，省略重复内容）
```

---

## HardwareMonitor.java （硬件监控）

> **作用**：基于 OSHI 库采集 CPU、内存、磁盘等实时数据，并决定是否需要持久化异常。

```java
// ...（完整源码同主 code.md 文件，省略重复内容）
```

---

## ExcelExporter.java （Excel导出）

> **作用**：将数据库中的异常性能数据导出为 Excel 文件，方便后期分析与答辩展示。

```java
// ...（完整源码同主 code.md 文件，省略重复内容）
```

---

## PerformanceData.java （性能数据模型）

> **作用**：封装每次采集的数据项，并内置判断是否为异常状态。

```java
// ...（完整源码同主 code.md 文件，省略重复内容）
```

---

## ConfigManager.java （配置管理）

> **作用**：负责加载、保存项目本地配置（如采集间隔、界面主题等）。

```java
// ...（完整源码同主 code.md 文件，省略重复内容）
```

---

## LogHelper.java （日志工具）

> **作用**：支持日志输出到控制台和日志文件，便于调试和追踪问题。

```java
// ...（完整源码同主 code.md 文件，省略重复内容）
```

---

## CpuInfoParser.java （CPU型号解析）

> **作用**：辅助解析 CPU 型号字符串，判断品牌、系列及核心数。

```java
// ...（完整源码同主 code.md 文件，省略重复内容）
```

---

## FileUtils.java （文件工具）

> **作用**：提供常用的文件操作方法（如检测、创建、删除、复制等）。

```java
// ...（完整源码同主 code.md 文件，省略重复内容）
```

---

[返回主导航页](code.md)
