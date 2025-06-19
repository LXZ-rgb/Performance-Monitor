---
layout: page
title: FXML 文件展示与界面说明
---

# 🖼️ FXML 文件 — JavaFX 界面布局

> 本页展示所有 JavaFX FXML 布局文件源码，并附以功能结构说明。  
> 这些文件位于 `performance-monitor/src/main/resources/ui/`。

---

## 目录

- [main_window.fxml](#main_windowfxml-主窗口界面)

---

## main_window.fxml （主窗口界面）

> **作用说明**：定义主监控窗口的布局，包括品牌Logo、性能曲线图、实时数据展示等。

> ![主界面预览](../assets/main_window_preview.png) <!-- 可换成你实际的界面截图 -->

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.image.*?>
<?import javafx.scene.chart.*?>

<BorderPane xmlns:fx="http://javafx.com/fxml" fx:controller="ui.MainController">
    <top>
        <HBox spacing="10" alignment="CENTER_LEFT" style="-fx-padding: 12;">
            <ImageView fx:id="brandLogoView" fitHeight="48" fitWidth="48"/>
            <VBox>
                <Label fx:id="cpuModelLabel" text="CPU 型号"/>
                <Label fx:id="diskModelLabel" text="磁盘型号"/>
            </VBox>
        </HBox>
    </top>
    <center>
        <LineChart fx:id="usageChart" title="性能变化曲线">
            <xAxis>
                <NumberAxis label="时间(s)"/>
            </xAxis>
            <yAxis>
                <NumberAxis label="使用率(%)"/>
            </yAxis>
        </LineChart>
    </center>
    <bottom>
        <HBox spacing="20" alignment="CENTER" style="-fx-padding: 12;">
            <Label text="CPU：" />
            <Label fx:id="cpuUsageLabel" text="0%" />
            <Label text="内存：" />
            <Label fx:id="memoryUsageLabel" text="0%" />
            <Label text="磁盘：" />
            <Label fx:id="diskUsageLabel" text="0%" />
            <Label text="温度：" />
            <Label fx:id="temperatureLabel" text="0°C" />
            <Button text="导出Excel" onAction="#handleExportExcel"/>
        </HBox>
    </bottom>
</BorderPane>
```

---

<div align="center" style="margin-top:2em;">
  <a href="code.md"><b>返回主导航页</b></a>
</div>
