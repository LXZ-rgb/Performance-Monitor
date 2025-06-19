---
layout: page
title: logic 包源码与结构详解
---

# 🧩 logic 包 — 数据、工具与业务逻辑

> 本页完整展示 logic 包下所有 Java 源码文件（含详细中文注释和作用说明），适合答辩讲解。**如需查看更多源码请[点击此处](https://github.com/LXZ-rgb/Performance-Monitor/search?q=repo%3ALXZ-rgb%2FPerformance-Monitor+path%3Aperformance-monitor%2Fsrc%2Fmain%2Fjava%2Flogic%2F+extension%3Ajava)**。

---

## 目录

- [DatabaseHandler.java](#databasehandlerjava-数据库操作)
- [HardwareMonitor.java](#hardwaremonitorjava-硬件监控)
- [ExcelExporter.java](#excelexporterjava-excel导出)
- [PerformanceData.java](#performancedatajava-性能数据模型)
- [StatisticsManager.java](#statisticsmanagerjava-数据统计)
- [PerformanceSimulator.java](#performancesimulatorjava-性能数据模拟)
- [LogHelper.java](#loghelperjava-日志工具)
- [CpuInfoParser.java](#cpuinfoparserjava-cpu型号解析)
- [FileUtils.java](#fileutilsjava-文件工具)
- [StringUtils.java](#stringutilsjava-字符串工具)
<!-- 如有其它文件补充 -->

---

## DatabaseHandler.java （数据库操作）

> **作用说明**：管理 SQLite 数据库连接、建表、数据存储与关闭。所有性能异常数据均由此管理持久化。



---

## HardwareMonitor.java （硬件监控）

> **作用说明**：基于 OSHI 库采集 CPU、内存、磁盘等实时数据，并决定是否需要持久化异常。



---

## 其它文件（以同样方式展开）

### ExcelExporter.java


### PerformanceData.java


### StatisticsManager.java


### PerformanceSimulator.java


### LogHelper.java


### CpuInfoParser.java


### FileUtils.java


### StringUtils.java


---
---

<div align="center" style="margin-top:2em;">
  <a href="code.md"><b>返回主导航页</b></a>
</div>
