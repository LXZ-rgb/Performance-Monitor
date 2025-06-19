---
layout: page
title: ui 包源码与结构详解
---

# 🎨 ui 包 — 主程序与控制器

> 本页完整展示 `ui` 包下所有 Java 源码（含中文注释和模块说明），并按功能分组解读。

---

## 目录

- [MainApp.java](#mainappjava-入口程序)
- [MainController.java](#maincontrollerjava-主界面控制器)
- [BrandLogoManager.java](#brandlogomanagerjava-品牌Logo管理)

---

## MainApp.java （入口程序）

> **作用说明**：JavaFX 应用主入口，负责加载主界面、异常日志记录等。

```java name=performance-monitor/src/main/java/ui/MainApp.java
package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            logException(e);
        });
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main_window.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("电脑性能监视器");
        primaryStage.setScene(new Scene(root, 800, 700));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    private static void logException(Throwable e) {
        try (FileWriter fw = new FileWriter("performance_monitor_error.log", true)) {
            fw.write(LocalDateTime.now() + ": Unhandled exception\n");
            fw.write("Message: " + e.getMessage() + "\n");
            for (StackTraceElement ste : e.getStackTrace()) {
                fw.write("\t" + ste.toString() + "\n");
            }
            fw.write("\n");
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }
}
```

---

## MainController.java （主界面控制器）

> **作用说明**：负责性能数据的实时显示、图表绘制、导出等主要界面交互逻辑。

```java name=performance-monitor/src/main/java/ui/MainController.java
package ui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import logic.HardwareMonitor;
import logic.HardwareMonitor.HardwareInfo;
import logic.PerformanceData;

public class MainController {
    @FXML private Label cpuModelLabel;
    @FXML private Label diskModelLabel;
    @FXML private Label cpuUsageLabel;
    @FXML private Label memoryUsageLabel;
    @FXML private Label diskUsageLabel;
    @FXML private Label temperatureLabel;
    @FXML private ImageView brandLogoView;
    @FXML private LineChart<Number, Number> usageChart;
    @FXML private CheckMenuItem cpuMenuItem;
    @FXML private CheckMenuItem memoryMenuItem;
    @FXML private CheckMenuItem diskMenuItem;

    private HardwareMonitor monitor;
    private BrandLogoManager logoManager;
    private AnimationTimer uiUpdateTimer;
    private XYChart.Series<Number, Number> cpuSeries;
    private XYChart.Series<Number, Number> memorySeries;
    private XYChart.Series<Number, Number> diskSeries;
    private int timeCounter = 0;
    private static final int MAX_DATA_POINTS = 60;

    private static final double CPU_THRESHOLD = 90.0;
    private static final double MEMORY_THRESHOLD = 85.0;
    private static final double DISK_THRESHOLD = 95.0;

    @FXML
    public void initialize() {
        try {
            initUsageChart();
            monitor = new HardwareMonitor();
            logoManager = new BrandLogoManager();
            displayHardwareInfo();
            monitor.startMonitoring(2);
            setupUIUpdateTimer();
            Stage stage = (Stage) cpuUsageLabel.getScene().getWindow();
            stage.setOnCloseRequest(this::handleWindowClose);
        } catch (Exception e) {
            e.printStackTrace();
            showError("初始化失败: " + e.getMessage());
        }
    }

    private void displayHardwareInfo() {
        try {
            HardwareInfo info = monitor.getHardwareInfo();
            cpuModelLabel.setText(info.cpuModel);
            diskModelLabel.setText(info.diskModel);
            String brand = logoManager.detectBrandFromModel(info.cpuModel);
            Image logo = logoManager.getBrandLogo(brand);
            if (logo != null) {
                brandLogoView.setImage(logo);
            }
        } catch (Exception e) {
            showError("无法获取硬件信息: " + e.getMessage());
        }
    }

    private void setupUIUpdateTimer() {
        uiUpdateTimer = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000L) {
                    updateUIWithRealData();
                    lastUpdate = now;
                }
            }
        };
        uiUpdateTimer.start();
    }

    private void updateUIWithRealData() {
        PerformanceData data = monitor.getLatestData();
        if (data != null) {
            updateUI(data);
        }
    }

    public void updateUI(PerformanceData data) {
        cpuUsageLabel.setText(String.format("%.1f%%", data.getCpuUsage()));
        memoryUsageLabel.setText(String.format("%.1f%%", data.getMemoryUsage()));
        diskUsageLabel.setText(String.format("%.1f%%", data.getDiskUsage()));
        temperatureLabel.setText(String.format("%.1f°C", data.getTemperature()));

        if (data.isAbnormal()) {
            cpuUsageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            memoryUsageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            diskUsageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            cpuUsageLabel.setStyle("");
            memoryUsageLabel.setStyle("");
            diskUsageLabel.setStyle("");
        }

        updateChart(data);
    }

    @FXML
    private void handleExportExcel() {
        try {
            logic.ExcelExporter.exportAbnormalData("performance_abnormal.xlsx");
            showInfo("数据已导出到 performance_abnormal.xlsx");
        } catch (Exception e) {
            showError("导出失败: " + e.getMessage());
        }
    }

    private void showError(String message) {
        System.err.println("[ERROR] " + message);
        cpuUsageLabel.setText("错误");
    }

    private void showInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    public void stopMonitoring() {
        if (monitor != null) monitor.stopMonitoring();
        if (uiUpdateTimer != null) uiUpdateTimer.stop();
    }

    @SuppressWarnings("unchecked")
    private void initUsageChart() {
        usageChart.getData().clear();
        cpuSeries = new XYChart.Series<>();
        cpuSeries.setName("CPU使用率");
        memorySeries = new XYChart.Series<>();
        memorySeries.setName("内存使用率");
        diskSeries = new XYChart.Series<>();
        diskSeries.setName("磁盘使用率");
        usageChart.getData().addAll(cpuSeries, memorySeries, diskSeries);
        updateChartVisibility();
    }

    private void updateChartVisibility() {
        if (cpuSeries != null) cpuSeries.getNode().setVisible(cpuMenuItem.isSelected());
        if (memorySeries != null) memorySeries.getNode().setVisible(memoryMenuItem.isSelected());
        if (diskSeries != null) diskSeries.getNode().setVisible(diskMenuItem.isSelected());
    }

    @FXML
    private void handleHardwareSelection() {
        updateChartVisibility();
    }

    @FXML
    private void handleResetChart() {
        if (cpuSeries != null) cpuSeries.getData().clear();
        if (memorySeries != null) memorySeries.getData().clear();
        if (diskSeries != null) diskSeries.getData().clear();
        timeCounter = 0;
    }

    private void handleWindowClose(WindowEvent event) {
        stopMonitoring();
        Platform.exit();
        System.exit(0);
    }

    private void updateChart(PerformanceData data) {
        if (cpuSeries == null || memorySeries == null || diskSeries == null) return;
        cpuSeries.getData().add(new XYChart.Data<>(timeCounter, data.getCpuUsage()));
        memorySeries.getData().add(new XYChart.Data<>(timeCounter, data.getMemoryUsage()));
        diskSeries.getData().add(new XYChart.Data<>(timeCounter, data.getDiskUsage()));

        if (cpuSeries.getData().size() > MAX_DATA_POINTS) cpuSeries.getData().remove(0);
        if (memorySeries.getData().size() > MAX_DATA_POINTS) memorySeries.getData().remove(0);
        if (diskSeries.getData().size() > MAX_DATA_POINTS) diskSeries.getData().remove(0);

        if (usageChart.getXAxis() instanceof NumberAxis) {
            NumberAxis xAxis = (NumberAxis) usageChart.getXAxis();
            xAxis.setLowerBound(Math.max(0, timeCounter - MAX_DATA_POINTS));
            xAxis.setUpperBound(timeCounter);
        }

        timeCounter++;
        updateChartColors(data);
    }

    private void updateChartColors(PerformanceData data) {
        if (data.getCpuUsage() > CPU_THRESHOLD)
            cpuSeries.getNode().setStyle("-fx-stroke: red;");
        else
            cpuSeries.getNode().setStyle("");
        if (data.getMemoryUsage() > MEMORY_THRESHOLD)
            memorySeries.getNode().setStyle("-fx-stroke: red;");
        else
            memorySeries.getNode().setStyle("");
        if (data.getDiskUsage() > DISK_THRESHOLD)
            diskSeries.getNode().setStyle("-fx-stroke: red;");
        else
            diskSeries.getNode().setStyle("");
    }
}
```

---

## BrandLogoManager.java （品牌Logo管理）

> **作用说明**：根据硬件型号智能切换品牌Logo，增强界面友好性。

```java name=performance-monitor/src/main/java/ui/BrandLogoManager.java
package ui;

import javafx.scene.image.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BrandLogoManager {
    private static final Map<String, String> BRAND_MAPPING = new HashMap<>();
    private static final Map<String, Image> LOGO_CACHE = new HashMap<>();
    static {
        BRAND_MAPPING.put("intel", "intel");
        BRAND_MAPPING.put("amd", "amd");
        BRAND_MAPPING.put("samsung", "samsung");
        BRAND_MAPPING.put("western digital", "wd");
        BRAND_MAPPING.put("seagate", "seagate");
        BRAND_MAPPING.put("kingston", "kingston");
    }

    public String detectBrandFromModel(String model) {
        if (model == null)
            return "default";
        String lowerModel = model.toLowerCase();
        for (Map.Entry<String, String> entry : BRAND_MAPPING.entrySet()) {
            if (lowerModel.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "default";
    }

    public Image getBrandLogo(String brand) {
        if (LOGO_CACHE.containsKey(brand))
            return LOGO_CACHE.get(brand);
        try {
            URL resourceUrl = getClass().getResource("/img/" + brand + "_logo.png");
            if (resourceUrl == null) {
                throw new Exception("品牌Logo资源未找到: " + brand);
            }
            Image logo = new Image(resourceUrl.toExternalForm());
            LOGO_CACHE.put(brand, logo);
            return logo;
        } catch (Exception e) {
            System.err.println("无法加载品牌Logo: " + brand + ", 使用默认Logo. 错误: " + e.getMessage());
            URL defaultUrl = getClass().getResource("/img/default_logo.png");
            if (defaultUrl != null) {
                Image defaultLogo = new Image(defaultUrl.toExternalForm());
                LOGO_CACHE.put("default", defaultLogo);
                return defaultLogo;
            } else {
                System.err.println("严重错误: 默认Logo也未找到!");
                return null;
            }
        }
    }
}
```

---

[返回主导航页](code.md)
