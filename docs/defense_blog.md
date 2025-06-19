---
layout: page
title: 电脑性能监视器项目答辩展示
---

# 🎓 电脑性能监视器 — 答辩展示专用博客

欢迎来到项目答辩专用页面！本页将以清晰、交互友好的博客风格，完整展示本项目的全部 Java 源码与 FXML 布局文件，并对各个模块进行结构化分组与简要讲解，帮助老师和同学们快速理解项目设计与实现思路。

---

## 🚩 项目结构总览

- **核心逻辑模块（logic 包）**  
  负责数据采集、数据库操作、配置、日志、工具等所有核心后端功能。
- **界面与交互模块（ui 包）**  
  包含主程序入口、控制器、品牌 Logo 管理与 FXML 界面定义，实现与用户的可视化交互。
- **资源文件（img/FXML）**  
  包括所有界面布局（FXML）和图标图片等静态资源。

---
## 🧩 1. 核心逻辑模块（logic 包）

### 1.1 DatabaseHandler.java

**作用：** SQLite数据库操作，负责性能数据的存储和表结构管理。

<details>
<summary>点击展开源码</summary>

```java
// ... 详细源码见 code.md ...
```
</details>

**说明：**  
- 自动在用户主目录下创建数据库文件。
- 支持性能数据的插入与表结构自建。

---

### 1.2 HardwareMonitor.java

**作用：** 采集硬件信息，包括CPU、内存、磁盘、温度等，并定时存数据库。

<details>
<summary>点击展开源码</summary>

```java
// ... 详细源码见 code.md ...
```
</details>

**说明：**  
- 使用 OSHI 框架跨平台采集硬件数据。
- 支持异常数据自动存储。

---

### 1.3 ExcelExporter.java

**作用：** 将性能异常数据导出为 Excel 文件，方便报告与分析。

<details>
<summary>点击展开源码</summary>

```java
// ... 详细源码见 code.md ...
```
</details>

---

### 1.4 PerformanceData.java

**作用：** 性能数据对象，包含判断数据异常的逻辑。

<details>
<summary>点击展开源码</summary>

```java
// ... 详细源码见 code.md ...
```
</details>

---

### 1.5 ConfigManager.java

**作用：** 应用配置文件的读取与保存。

<details>
<summary>点击展开源码</summary>

```java
// ... 详细源码见 code.md ...
```
</details>

---

### 1.6 LogHelper.java

**作用：** 日志输出工具，支持控制台与文件。

<details>
<summary>点击展开源码</summary>

```java
// ... 详细源码见 code.md ...
```
</details>

---

### 1.7 CpuInfoParser.java

**作用：** 解析 CPU 型号字符串，提取品牌、系列、核心数等。

<details>
<summary>点击展开源码</summary>

```java
// ... 详细源码见 code.md ...
```
</details>

---

### 1.8 FileUtils.java

**作用：** 常用文件操作方法（判断/复制/删除/新建/文件夹等）。

<details>
<summary>点击展开源码</summary>

```java
// ... 详细源码见 code.md ...
```
</details>

---

## 🖼️ 2. 界面与控制器模块（ui 包）

### 2.1 MainApp.java

**作用：** JavaFX 主程序入口，负责窗口初始化和全局异常日志。

<details>
<summary>点击展开源码</summary>

```java
// ... 详细源码见 code.md ...
```
</details>

---

### 2.2 MainController.java

**作用：** JavaFX 控制器，负责界面与性能数据的实时交互、图表显示、导出等。

<details>
<summary>点击展开源码</summary>

```java
// ... 详细源码见 code.md ...
```
</details>

---

### 2.3 BrandLogoManager.java

**作用：** 硬件品牌识别与 Logo 加载，增强视觉识别度。

<details>
<summary>点击展开源码</summary>

```java
// ... 详细源码见 code.md ...
```
</details>

---

## 🎨 3. FXML 界面布局文件

### 3.1 main_window.fxml

**作用：** 项目主界面布局文件，定义了所有可视化控件和布局方式。

<details>
<summary>点击展开 FXML 源码</summary>

```xml
<!-- 实际源码为示例，需根据项目 /ui/main_window.fxml 文件粘贴 -->
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.scene.control.*?>
<?import javafx.scene.image.ImageView?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.chart.*?>

<BorderPane xmlns:fx="http://javafx.com/fxml" fx:controller="ui.MainController">
    <top>
        <MenuBar>
            <Menu text="文件">
                <MenuItem text="导出Excel" onAction="#handleExportExcel"/>
                <MenuItem text="重置图表" onAction="#handleResetChart"/>
                <SeparatorMenuItem/>
                <MenuItem text="退出" onAction="#handleWindowClose"/>
            </Menu>
            <Menu text="显示">
                <CheckMenuItem fx:id="cpuMenuItem" text="CPU" selected="true" onAction="#handleHardwareSelection"/>
                <CheckMenuItem fx:id="memoryMenuItem" text="内存" selected="true" onAction="#handleHardwareSelection"/>
                <CheckMenuItem fx:id="diskMenuItem" text="磁盘" selected="true" onAction="#handleHardwareSelection"/>
            </Menu>
        </MenuBar>
    </top>
    <center>
        <VBox spacing="15" alignment="CENTER">
            <HBox spacing="20" alignment="CENTER">
                <ImageView fx:id="brandLogoView" fitHeight="48" fitWidth="48"/>
                <VBox>
                    <Label text="CPU型号：" />
                    <Label fx:id="cpuModelLabel"/>
                    <Label text="硬盘型号：" />
                    <Label fx:id="diskModelLabel"/>
                </VBox>
            </HBox>
            <HBox spacing="30" alignment="CENTER">
                <Label text="CPU使用率：" />
                <Label fx:id="cpuUsageLabel" style="-fx-font-size: 18px;"/>
                <Label text="内存使用率：" />
                <Label fx:id="memoryUsageLabel" style="-fx-font-size: 18px;"/>
                <Label text="磁盘使用率：" />
                <Label fx:id="diskUsageLabel" style="-fx-font-size: 18px;"/>
                <Label text="温度：" />
                <Label fx:id="temperatureLabel" style="-fx-font-size: 18px;"/>
            </HBox>
            <LineChart fx:id="usageChart" title="性能趋势" prefWidth="700" prefHeight="320">
                <xAxis>
                    <NumberAxis label="时间"/>
                </xAxis>
                <yAxis>
                    <NumberAxis label="使用率/温度"/>
                </yAxis>
            </LineChart>
        </VBox>
    </center>
</BorderPane>
```
</details>

---

## 🗂️ 4. 资源文件（img/logo）

- `img/intel_logo.png`、`img/amd_logo.png`、`img/default_logo.png` 等
- 用于品牌展示，提升界面美观度与专业性

---

## 📝 总结

- 以上即为本项目所有源码、主要 FXML 布局文件及结构化说明。
- 各模块职责清晰，前后端分离，易扩展与维护。
- 如需源码高亮展示、交互折叠或代码行号展示，可结合 Markdown 博客平台自定义样式进一步美化。

---

> 如需补充其它模块源码或详细某一部分讲解，请随时留言！
