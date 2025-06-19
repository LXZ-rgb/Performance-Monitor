---
layout: page
title: ui 包源码与界面展示
---

# 🎨 界面层（ui 包）源码与 JavaFX/FXML 展示

> 本页完整展示 ui 包下所有 Java 控制器、界面工具类，以及相关 FXML 布局文件源码。每个模块都带有功能说明与详细注释，适合答辩讲解和查阅。

---

## 目录

- [MainApp.java - 应用主入口](#mainappjava---应用主入口)
- [MainController.java - 主界面控制器](#maincontrollerjava---主界面控制器)
- [BrandLogoManager.java - 品牌Logo管理](#brandlogomanagerjava---品牌logo管理)
- [main_window.fxml - 主窗口界面](#main_windowfxml---主窗口界面)
<!-- 如有其它控制器/FXML也请补充 -->

---

## MainApp.java - 应用主入口

> **功能说明**：JavaFX 应用程序启动入口，负责加载 FXML 布局、显示主窗口、全局异常处理等。

```java
package ui;

import javafx.application.Application; // 导入JavaFX Application基类
import javafx.fxml.FXMLLoader;         // 导入FXML加载器
import javafx.scene.Parent;            // 导入JavaFX节点根类
import javafx.scene.Scene;             // 导入JavaFX场景
import javafx.stage.Stage;             // 导入JavaFX窗口

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 应用程序主入口类，负责JavaFX启动与异常处理
 */
public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // 设置全局未捕获异常处理器，异常写入日志
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            logException(e);
        });
        // 加载主界面FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main_window.fxml"));
        Parent root = loader.load();
        // 设置窗口标题和尺寸
        primaryStage.setTitle("电脑性能监视器");
        primaryStage.setScene(new Scene(root, 800, 700));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // 启动JavaFX应用
    }

    @Override
    public void stop() {
        System.exit(0); // 强制退出，确保所有线程关闭
    }

    // 静态方法：将异常写入日志文件
    private static void logException(Throwable e) {
        try (FileWriter fw = new FileWriter("performance_monitor_error.log", true)) {
            fw.write(LocalDateTime.now() + ": Unhandled exception\n");
            fw.write("Message: " + e.getMessage() + "\n");
            for (StackTraceElement ste : e.getStackTrace()) {
                fw.write("\t" + ste.toString() + "\n");
            }
            fw.write("\n");
        } catch (IOException ex) {
            // 忽略日志写入异常
        }
    }
}
```

---

## MainController.java - 主界面控制器

> **功能说明**：负责主界面所有数据交互（如性能数据刷新、按钮操作、图表显示等）。

```java
package ui;

import javafx.animation.AnimationTimer;          // 动画计时器，定时刷新UI
import javafx.application.Platform;             // 平台线程工具
import javafx.fxml.FXML;                        // FXML注解，绑定UI元素
import javafx.scene.control.Label;              // 标签控件
import javafx.scene.image.Image;                // 图片类型
import javafx.scene.image.ImageView;            // 图片显示控件
import javafx.scene.chart.LineChart;            // 折线图控件
import javafx.scene.chart.NumberAxis;           // 数字坐标轴
import javafx.scene.chart.XYChart;              // 折线图数据结构
import logic.HardwareMonitor;                   // 性能监控逻辑
import logic.HardwareMonitor.HardwareInfo;      // 硬件信息内部类
import logic.PerformanceData;                   // 性能数据类

/**
 * 主窗口控制器，负责性能数据采集、UI数据绑定等
 */
public class MainController {
    @FXML private Label cpuModelLabel;
    @FXML private Label diskModelLabel;
    @FXML private Label cpuUsageLabel;
    @FXML private Label memoryUsageLabel;
    @FXML private Label diskUsageLabel;
    @FXML private Label temperatureLabel;
    @FXML private ImageView brandLogoView;
    @FXML private LineChart<Number, Number> usageChart;

    private HardwareMonitor monitor;         // 性能监控逻辑对象
    private AnimationTimer uiUpdateTimer;    // UI定时刷新

    private XYChart.Series<Number, Number> cpuSeries;
    private XYChart.Series<Number, Number> memorySeries;
    private XYChart.Series<Number, Number> diskSeries;

    public void initialize() {
        monitor = new HardwareMonitor();
        HardwareInfo info = monitor.getHardwareInfo();
        cpuModelLabel.setText(info.cpuModel);
        diskModelLabel.setText(info.diskModel);

        // 初始化折线图
        cpuSeries = new XYChart.Series<>();
        cpuSeries.setName("CPU");
        memorySeries = new XYChart.Series<>();
        memorySeries.setName("内存");
        diskSeries = new XYChart.Series<>();
        diskSeries.setName("磁盘");
        usageChart.getData().addAll(cpuSeries, memorySeries, diskSeries);

        // 启动定时刷新
        uiUpdateTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateUI();
            }
        };
        uiUpdateTimer.start();
    }

    // 更新UI显示
    private void updateUI() {
        PerformanceData data = monitor.getLatestData();
        if (data == null) return;
        cpuUsageLabel.setText(String.format("%.1f%%", data.getCpuUsage()));
        memoryUsageLabel.setText(String.format("%.1f%%", data.getMemoryUsage()));
        diskUsageLabel.setText(String.format("%.1f%%", data.getDiskUsage()));
        temperatureLabel.setText(String.format("%.1f°C", data.getTemperature()));

        // 图表滚动追加数据
        int point = cpuSeries.getData().size();
        cpuSeries.getData().add(new XYChart.Data<>(point, data.getCpuUsage()));
        memorySeries.getData().add(new XYChart.Data<>(point, data.getMemoryUsage()));
        diskSeries.getData().add(new XYChart.Data<>(point, data.getDiskUsage()));
        if (cpuSeries.getData().size() > 60) { // 只保留60点
            cpuSeries.getData().remove(0);
            memorySeries.getData().remove(0);
            diskSeries.getData().remove(0);
        }
    }

    // 导出Excel按钮事件
    @FXML
    private void handleExportExcel() {
        // 省略：调用ExcelExporter导出功能
    }
}
```

---

## BrandLogoManager.java - 品牌Logo管理

> **功能说明**：根据硬件品牌自动切换显示对应Logo图片。

```java
package ui;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

/**
 * 品牌Logo管理器，根据硬件型号动态匹配显示Logo图片
 */
public class BrandLogoManager {
    private final Map<String, Image> logoMap;

    public BrandLogoManager() {
        logoMap = new HashMap<>();
        // 预加载常见品牌Logo
        logoMap.put("Intel", loadLogo("/ui/logo/intel.png"));
        logoMap.put("AMD", loadLogo("/ui/logo/amd.png"));
        // 可继续添加其它品牌
    }

    public String detectBrandFromModel(String model) {
        if (model == null) return "";
        model = model.toLowerCase();
        if (model.contains("intel")) return "Intel";
        if (model.contains("amd")) return "AMD";
        // ...可扩展更多品牌
        return "";
    }

    public Image getBrandLogo(String brand) {
        return logoMap.getOrDefault(brand, null);
    }

    private Image loadLogo(String path) {
        try {
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("Logo加载失败: " + path);
            return null;
        }
    }
}
```

---

## main_window.fxml - 主窗口界面

> **功能说明**：定义主监控窗口布局，包括Logo、性能折线图、实时数据等。

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
