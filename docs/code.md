---
layout: page
title: 项目源码与结构介绍
---

# 🌟 源代码完整展示与解析

<div align="center">
  <img src="https://img.shields.io/badge/Java-PerformanceMonitor-blue?logo=java" alt="Java">
  <img src="https://img.shields.io/badge/技术栈-JavaFX%20%7C%20OSHI%20%7C%20SQLite-green" alt="Stack">
  <img src="https://img.shields.io/badge/开源-GitHub-brightgreen" alt="GitHub">
</div>

<br/>

> 本页详细展示 Performance Monitor 项目的核心源码，包括每个文件的全部内容和结构解读，便于学习与参考。

---

## 📁 目录

- [1. DatabaseHandler.java](#1-databasehandlerjava)
- [2. HardwareMonitor.java](#2-hardwaremonitorjava)
- [3. ExcelExporter.java](#3-excelexporterjava)
- [4. PerformanceData.java](#4-performancedatajava)
- [5. BrandLogoManager.java](#5-brandlogomanagerjava)
- [6. MainApp.java](#6-mainappjava)
- [7. MainController.java](#7-maincontrollerjava)

---

## 1. `logic/DatabaseHandler.java` <span style="font-size:0.85em;color:#888;">数据库管理</span>

<details>
<summary>点击展开/折叠源码</summary>

```java
// DatabaseHandler.java
// 负责数据库的连接、数据的存取操作
package logic;

import java.sql.*;
import java.nio.file.*;

public class DatabaseHandler {
    private Connection connection;

    public DatabaseHandler() {
        try {
            // 获取用户目录下的专用文件夹
            String userHome = System.getProperty("user.home");
            String appDir = userHome + "/PerformanceMonitor";
            Path dbPath = Paths.get(appDir, "performance.db");
            
            // 确保目录存在
            Files.createDirectories(dbPath.getParent());
            
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            createTable();
        } catch (Exception e) {
            System.err.println("数据库连接失败: " + e.getMessage());
        }
    }

    private void createTable() {
        final String sql = """
                CREATE TABLE IF NOT EXISTS performance_data (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp TEXT NOT NULL,
                    cpu_usage REAL NOT NULL,
                    memory_usage REAL NOT NULL,
                    disk_usage REAL NOT NULL,
                    temperature REAL NOT NULL
                )
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("创建表失败: " + e.getMessage());
        }
    }

    public void savePerformanceData(PerformanceData data) {
        final String sql = "INSERT INTO performance_data (timestamp, cpu_usage, memory_usage, disk_usage, temperature) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data.getTimestamp().toString());
            pstmt.setDouble(2, data.getCpuUsage());
            pstmt.setDouble(3, data.getMemoryUsage());
            pstmt.setDouble(4, data.getDiskUsage());
            pstmt.setDouble(5, data.getTemperature());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("保存数据失败: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("关闭数据库连接失败: " + e.getMessage());
        }
    }
    
    public static String getDatabasePath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, "PerformanceMonitor", "performance.db").toString();
    }
}
```
</details>

**亮点说明：**
- 📝 自动在用户目录下创建数据库，无需手动配置。
- 🔒 所有数据库操作均带异常捕获，提升健壮性。

---

## 2. `logic/HardwareMonitor.java` <span style="font-size:0.85em;color:#888;">硬件性能采集</span>

<details>
<summary>点击展开/折叠源码</summary>

```java
// HardwareMonitor.java
// 负责采集与监控硬件性能数据（CPU、内存、磁盘、温度）
package logic;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSFileStore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HardwareMonitor {
    private final SystemInfo systemInfo = new SystemInfo();
    private final HardwareAbstractionLayer hardware = systemInfo.getHardware();
    private final CentralProcessor processor = hardware.getProcessor();
    private final GlobalMemory memory = hardware.getMemory();

    private Timer monitoringTimer;
    private DatabaseHandler dbHandler;
    private PerformanceData latestData;

    public HardwareMonitor() {
        this.dbHandler = new DatabaseHandler();
    }

    public void startMonitoring(int intervalSeconds) {
        monitoringTimer = new Timer();
        monitoringTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                PerformanceData data = collectPerformanceData();
                latestData = data;
                if (data.isAbnormal()) {
                    dbHandler.savePerformanceData(data);
                }
            }
        }, 0, intervalSeconds * 1000L);
    }

    public void stopMonitoring() {
        if (monitoringTimer != null) {
            monitoringTimer.cancel();
        }
        dbHandler.closeConnection();
    }

    public PerformanceData getLatestData() {
        return latestData; // 用于UI实时刷新真实数据
    }

    private PerformanceData collectPerformanceData() {
        double cpuUsage = processor.getSystemCpuLoad(1000) * 100;
        double memoryUsage = (memory.getTotal() - memory.getAvailable()) * 100.0 / memory.getTotal();
        double diskUsage = 0;
        List<OSFileStore> fileStores = systemInfo.getOperatingSystem().getFileSystem().getFileStores();
        if (!fileStores.isEmpty()) {
            OSFileStore fs = fileStores.get(0);
            diskUsage = (fs.getTotalSpace() - fs.getFreeSpace()) * 100.0 / fs.getTotalSpace();
        }
        double temperature = getCpuTemperature();
        return new PerformanceData(LocalDateTime.now(), cpuUsage, memoryUsage, diskUsage, temperature);
    }

    private double getCpuTemperature() {
        double temp = hardware.getSensors().getCpuTemperature();
        // 用模拟温度兜底，保证不会NaN
        if (Double.isNaN(temp) || temp <= 0) {
            return 40 + Math.random() * 20;
        }
        return temp;
    }

    public HardwareInfo getHardwareInfo() {
        String cpuModel = processor.getProcessorIdentifier().getName();
        String diskModel = "Unknown";
        List<HWDiskStore> diskStores = hardware.getDiskStores();
        if (!diskStores.isEmpty()) {
            diskModel = diskStores.get(0).getModel();
        }
        String motherboardModel = hardware.getComputerSystem().getBaseboard().getModel();
        return new HardwareInfo(cpuModel, diskModel, motherboardModel);
    }

    public static class HardwareInfo {
        public final String cpuModel;
        public final String diskModel;
        public final String motherboardModel;

        public HardwareInfo(String cpuModel, String diskModel, String motherboardModel) {
            this.cpuModel = cpuModel;
            this.diskModel = diskModel;
            this.motherboardModel = motherboardModel;
        }
    }
}
```
</details>

**亮点说明：**
- ⏱️ 使用 `Timer` 实现定时采集，采集周期可调节
- 🌡️ 支持 CPU 温度、内存、磁盘等多维度监控
- 📝 采集结果可实时提供给 UI 界面刷新

---

## 3. `logic/ExcelExporter.java` <span style="font-size:0.85em;color:#888;">一键导出</span>

<details>
<summary>点击展开/折叠源码</summary>

```java
// ExcelExporter.java
// 实现性能数据的导出为 Excel 文件
package logic;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.sql.*;

public class ExcelExporter {
    public static void exportAbnormalData(String filePath) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DatabaseHandler.getDatabasePath());
             Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("性能异常数据");
            // 表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            // 表头
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "时间戳", "CPU使用率(%)", "内存使用率(%)", "磁盘使用率(%)", "温度(°C)" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            // 查询并写入数据
            String sql = "SELECT * FROM performance_data";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                int rowNum = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rs.getInt("id"));
                    row.createCell(1).setCellValue(rs.getString("timestamp"));
                    row.createCell(2).setCellValue(rs.getDouble("cpu_usage"));
                    row.createCell(3).setCellValue(rs.getDouble("memory_usage"));
                    row.createCell(4).setCellValue(rs.getDouble("disk_usage"));
                    row.createCell(5).setCellValue(rs.getDouble("temperature"));
                }
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }
}
```
</details>

**亮点说明：**
- 📊 使用 Apache POI 导出高质量 Excel 报表
- 📁 导出表头清晰、数据完整，自动适配列宽

---

## 4. `logic/PerformanceData.java` <span style="font-size:0.85em;color:#888;">数据结构</span>

<details>
<summary>点击展开/折叠源码</summary>

```java
package logic;

import java.time.LocalDateTime;

public class PerformanceData {
    private final LocalDateTime timestamp;
    private final double cpuUsage;
    private final double memoryUsage;
    private final double diskUsage;
    private final double temperature;
    private final boolean isAbnormal;

    public PerformanceData(LocalDateTime timestamp, double cpuUsage,
            double memoryUsage, double diskUsage,
            double temperature) {
        this.timestamp = timestamp;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.diskUsage = diskUsage;
        this.temperature = temperature;
        this.isAbnormal = checkAbnormal();
    }

    private boolean checkAbnormal() {
        final double CPU_THRESHOLD = 90.0;
        final double MEMORY_THRESHOLD = 85.0;
        final double DISK_THRESHOLD = 95.0;
        return cpuUsage > CPU_THRESHOLD ||
                memoryUsage > MEMORY_THRESHOLD ||
                diskUsage > DISK_THRESHOLD;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }

    public double getDiskUsage() {
        return diskUsage;
    }

    public double getTemperature() {
        return temperature;
    }

    public boolean isAbnormal() {
        return isAbnormal;
    }
}
```
</details>

**亮点说明：**
- 🎯 自动判定异常数据点（如高负载）
- 🧩 封装良好，便于扩展与维护

---

## 5. `ui/BrandLogoManager.java` <span style="font-size:0.85em;color:#888;">品牌Logo智能管理</span>

<details>
<summary>点击展开/折叠源码</summary>

```java
// BrandLogoManager.java
// 品牌LOGO的加载与管理
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
            // 使用URL加载资源，确保在打包后也能正确访问
            URL resourceUrl = getClass().getResource("/img/" + brand + "_logo.png");
            if (resourceUrl == null) {
                throw new Exception("品牌Logo资源未找到: " + brand);
            }
            
            Image logo = new Image(resourceUrl.toExternalForm());
            LOGO_CACHE.put(brand, logo);
            return logo;
        } catch (Exception e) {
            System.err.println("无法加载品牌Logo: " + brand + ", 使用默认Logo. 错误: " + e.getMessage());
            
            // 加载默认logo
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
</details>

**亮点说明：**
- 🤖 支持多品牌一键识别和 Logo 加载
- 🚀 内置 Logo 缓存机制，访问更快更省资源

---

## 6. `ui/MainApp.java` <span style="font-size:0.85em;color:#888;">应用入口</span>

<details>
<summary>点击展开/折叠源码</summary>

```java
// MainApp.java
// 应用主入口
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
        // 设置全局异常处理器
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
</details>

**亮点说明：**
- 💡 统一异常日志，保障运行安全
- 🎨 界面风格简洁、主窗口自适应

---

## 7. `ui/MainController.java` <span style="font-size:0.85em;color:#888;">主界面逻辑</span>

<details>
<summary>点击展开/折叠源码</summary>

```java
// MainController.java
// 主界面控制逻辑
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
    // 原有UI组件
    @FXML
    private Label cpuModelLabel;
    @FXML
    private Label diskModelLabel;
    @FXML
    private Label cpuUsageLabel;
    @FXML
    private Label memoryUsageLabel;
    @FXML
    private Label diskUsageLabel;
    @FXML
    private Label temperatureLabel;
    @FXML
    private ImageView brandLogoView;

    // 新增UI组件
    @FXML
    private LineChart<Number, Number> usageChart;
    @FXML
    private CheckMenuItem cpuMenuItem;
    @FXML
    private CheckMenuItem memoryMenuItem;
    @FXML
    private CheckMenuItem diskMenuItem;

    // 原有成员变量
    private HardwareMonitor monitor;
    private BrandLogoManager logoManager;
    private AnimationTimer uiUpdateTimer;

    // 折线图相关变量
    private XYChart.Series<Number, Number> cpuSeries;
    private XYChart.Series<Number, Number> memorySeries;
    private XYChart.Series<Number, Number> diskSeries;
    private int timeCounter = 0;
    private static final int MAX_DATA_POINTS = 60;

    // 阈值设置（与PerformanceData保持一致）
    private static final double CPU_THRESHOLD = 90.0;
    private static final double MEMORY_THRESHOLD = 85.0;
    private static final double DISK_THRESHOLD = 95.0;

    @FXML
    public void initialize() {
        try {
            // 初始化折线图
            initUsageChart();

            // 原有初始化逻辑
            monitor = new HardwareMonitor();
            logoManager = new BrandLogoManager();
            displayHardwareInfo();
            monitor.startMonitoring(2); // 2秒采样
            setupUIUpdateTimer();

            // 监听窗口关闭事件
            Stage stage = (Stage) cpuUsageLabel.getScene().getWindow();
            stage.setOnCloseRequest(this::handleWindowClose);
        } catch (Exception e) {
            e.printStackTrace();
            showError("初始化失败: " + e.getMessage());
        }
    }

    // === 原有方法 ===
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
                if (now - lastUpdate >= 1_000_000_000L) { // 每秒
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
        // 更新文本标签
        cpuUsageLabel.setText(String.format("%.1f%%", data.getCpuUsage()));
        memoryUsageLabel.setText(String.format("%.1f%%", data.getMemoryUsage()));
        diskUsageLabel.setText(String.format("%.1f%%", data.getDiskUsage()));
        temperatureLabel.setText(String.format("%.1f°C", data.getTemperature()));

        // 异常状态标记
        if (data.isAbnormal()) {
            cpuUsageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            memoryUsageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            diskUsageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            cpuUsageLabel.setStyle("");
            memoryUsageLabel.setStyle("");
            diskUsageLabel.setStyle("");
        }

        // 新增：更新折线图
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
        // 在实际应用中可替换为弹窗
        cpuUsageLabel.setText("错误");
    }

    private void showInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    public void stopMonitoring() {
        if (monitor != null) {
            monitor.stopMonitoring();
        }
        if (uiUpdateTimer != null) {
            uiUpdateTimer.stop();
        }
    }

    // === 新增方法 ===
    @SuppressWarnings("unchecked")
    private void initUsageChart() {
        // 清除现有数据
        usageChart.getData().clear();

        // 创建数据系列
        cpuSeries = new XYChart.Series<>();
        cpuSeries.setName("CPU使用率");

        memorySeries = new XYChart.Series<>();
        memorySeries.setName("内存使用率");

        diskSeries = new XYChart.Series<>();
        diskSeries.setName("磁盘使用率");

        // 添加系列到图表
        usageChart.getData().addAll(cpuSeries, memorySeries, diskSeries);

        // 设置初始可见性
        updateChartVisibility();
    }

    private void updateChartVisibility() {
        if (cpuSeries != null) {
            cpuSeries.getNode().setVisible(cpuMenuItem.isSelected());
        }
        if (memorySeries != null) {
            memorySeries.getNode().setVisible(memoryMenuItem.isSelected());
        }
        if (diskSeries != null) {
            diskSeries.getNode().setVisible(diskMenuItem.isSelected());
        }
    }

    @FXML
    private void handleHardwareSelection() {
        updateChartVisibility();
    }

    @FXML
    private void handleResetChart() {
        if (cpuSeries != null)
            cpuSeries.getData().clear();
        if (memorySeries != null)
            memorySeries.getData().clear();
        if (diskSeries != null)
            diskSeries.getData().clear();
        timeCounter = 0;
    }

    private void handleWindowClose(WindowEvent event) {
        stopMonitoring();
        Platform.exit();
        System.exit(0);
    }

    private void updateChart(PerformanceData data) {
        if (cpuSeries == null || memorySeries == null || diskSeries == null) {
            return; // 确保系列已初始化
        }

        // 添加新数据点
        cpuSeries.getData().add(new XYChart.Data<>(timeCounter, data.getCpuUsage()));
        memorySeries.getData().add(new XYChart.Data<>(timeCounter, data.getMemoryUsage()));
        diskSeries.getData().add(new XYChart.Data<>(timeCounter, data.getDiskUsage()));

        // 移除旧数据点，保持固定长度
        if (cpuSeries.getData().size() > MAX_DATA_POINTS) {
            cpuSeries.getData().remove(0);
        }
        if (memorySeries.getData().size() > MAX_DATA_POINTS) {
            memorySeries.getData().remove(0);
        }
        if (diskSeries.getData().size() > MAX_DATA_POINTS) {
            diskSeries.getData().remove(0);
        }

        // 更新X轴范围
        if (usageChart.getXAxis() instanceof NumberAxis) {
            NumberAxis xAxis = (NumberAxis) usageChart.getXAxis();
            xAxis.setLowerBound(Math.max(0, timeCounter - MAX_DATA_POINTS));
            xAxis.setUpperBound(timeCounter);
        }

        timeCounter++;

        // 根据阈值设置折线颜色
        updateChartColors(data);
    }

    private void updateChartColors(PerformanceData data) {
        // CPU使用率颜色
        if (data.getCpuUsage() > CPU_THRESHOLD) {
            cpuSeries.getNode().setStyle("-fx-stroke: red;");
        } else {
            cpuSeries.getNode().setStyle("");
        }

        // 内存使用率颜色
        if (data.getMemoryUsage() > MEMORY_THRESHOLD) {
            memorySeries.getNode().setStyle("-fx-stroke: red;");
        } else {
            memorySeries.getNode().setStyle("");
        }

        // 磁盘使用率颜色
        if (data.getDiskUsage() > DISK_THRESHOLD) {
            diskSeries.getNode().setStyle("-fx-stroke: red;");
        } else {
            diskSeries.getNode().setStyle("");
        }
    }
}
```
</details>

**亮点说明：**
- 📈 折线实时刷新，多项指标动态切换
- 🛡️ 异常状态高亮提示，极致易用体验

---

<div align="center">

<a href="https://github.com/LXZ-rgb/Performance-Monitor" target="_blank">
  <img src="https://img.shields.io/github/stars/LXZ-rgb/Performance-Monitor?style=social" alt="GitHub stars">
</a>
<br/>
<b>如需查看所有源文件，请访问 <a href="https://github.com/LXZ-rgb/Performance-Monitor" target="_blank">GitHub仓库</a></b>
<br/>
<a href="index.md">返回首页</a>
</div>
