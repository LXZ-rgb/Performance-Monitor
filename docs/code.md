---
layout: page
title: 项目源码与结构介绍
---

# 🌟 源代码完整展示与逐行注释

<div align="center">
  <img src="https://img.shields.io/badge/Java-PerformanceMonitor-blue?logo=java" alt="Java">
  <img src="https://img.shields.io/badge/技术栈-JavaFX%20%7C%20OSHI%20%7C%20SQLite-green" alt="Stack">
  <img src="https://img.shields.io/badge/开源-GitHub-brightgreen" alt="GitHub">
</div>

<br/>

> 本页面详细展示 Performance Monitor 项目的核心源码（含完整中文注释），便于学习与答辩使用。

---

## 1. `logic/DatabaseHandler.java` <span style="font-size:0.85em;color:#888;">数据库管理</span>

负责数据库的连接、初始化以及性能数据的存储。

```java
package logic;

import java.sql.*;
import java.nio.file.*;

public class DatabaseHandler {
    private Connection connection; // 数据库连接对象

    public DatabaseHandler() {
        try {
            // 获取用户家目录
            String userHome = System.getProperty("user.home");
            // 拼接应用文件夹路径
            String appDir = userHome + "/PerformanceMonitor";
            // 拼接数据库文件路径
            Path dbPath = Paths.get(appDir, "performance.db");
            
            // 创建数据库所在文件夹（如果不存在）
            Files.createDirectories(dbPath.getParent());
            
            // 加载SQLite驱动
            Class.forName("org.sqlite.JDBC");
            // 建立数据库连接
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            // 创建数据表（如不存在）
            createTable();
        } catch (Exception e) {
            // 捕获并输出异常
            System.err.println("数据库连接失败: " + e.getMessage());
        }
    }

    // 创建性能数据表
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
            stmt.execute(sql); // 执行建表语句
        } catch (SQLException e) {
            System.err.println("创建表失败: " + e.getMessage());
        }
    }

    // 保存一条性能数据
    public void savePerformanceData(PerformanceData data) {
        final String sql = "INSERT INTO performance_data (timestamp, cpu_usage, memory_usage, disk_usage, temperature) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, data.getTimestamp().toString()); // 时间戳
            pstmt.setDouble(2, data.getCpuUsage());              // CPU使用率
            pstmt.setDouble(3, data.getMemoryUsage());           // 内存使用率
            pstmt.setDouble(4, data.getDiskUsage());             // 磁盘使用率
            pstmt.setDouble(5, data.getTemperature());           // 温度
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("保存数据失败: " + e.getMessage());
        }
    }

    // 关闭数据库连接
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("关闭数据库连接失败: " + e.getMessage());
        }
    }
    
    // 静态方法：获取数据库路径
    public static String getDatabasePath() {
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, "PerformanceMonitor", "performance.db").toString();
    }
}
```

**亮点说明：**
- 📝 自动在用户目录下创建数据库，无需手动配置。
- 🔒 所有数据库操作均带异常捕获，提升健壮性。

---

## 2. `logic/HardwareMonitor.java` <span style="font-size:0.85em;color:#888;">硬件性能采集</span>

负责采集CPU、内存、磁盘和温度等硬件性能数据。

```java
package logic;

import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.OSFileStore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HardwareMonitor {
    // OSHI库获取系统硬件信息
    private final SystemInfo systemInfo = new SystemInfo();
    private final HardwareAbstractionLayer hardware = systemInfo.getHardware();
    private final CentralProcessor processor = hardware.getProcessor();
    private final GlobalMemory memory = hardware.getMemory();

    private Timer monitoringTimer;              // 定时任务
    private DatabaseHandler dbHandler;          // 数据库操作对象
    private PerformanceData latestData;         // 最新数据

    public HardwareMonitor() {
        this.dbHandler = new DatabaseHandler(); // 初始化数据库
    }

    // 启动定时监控
    public void startMonitoring(int intervalSeconds) {
        monitoringTimer = new Timer();
        monitoringTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                PerformanceData data = collectPerformanceData(); // 采集性能数据
                latestData = data;
                if (data.isAbnormal()) { // 如果是异常数据则保存
                    dbHandler.savePerformanceData(data);
                }
            }
        }, 0, intervalSeconds * 1000L); // 设置采样周期
    }

    // 停止监控并关闭数据库连接
    public void stopMonitoring() {
        if (monitoringTimer != null) {
            monitoringTimer.cancel();
        }
        dbHandler.closeConnection();
    }

    // 获取最新采集数据，供UI实时刷新
    public PerformanceData getLatestData() {
        return latestData;
    }

    // 采集性能数据
    private PerformanceData collectPerformanceData() {
        double cpuUsage = processor.getSystemCpuLoad(1000) * 100; // CPU使用率
        double memoryUsage = (memory.getTotal() - memory.getAvailable()) * 100.0 / memory.getTotal(); // 内存
        double diskUsage = 0;
        List<OSFileStore> fileStores = systemInfo.getOperatingSystem().getFileSystem().getFileStores();
        if (!fileStores.isEmpty()) {
            OSFileStore fs = fileStores.get(0);
            diskUsage = (fs.getTotalSpace() - fs.getFreeSpace()) * 100.0 / fs.getTotalSpace(); // 磁盘
        }
        double temperature = getCpuTemperature(); // CPU温度
        return new PerformanceData(LocalDateTime.now(), cpuUsage, memoryUsage, diskUsage, temperature);
    }

    // 获取CPU温度，如无则生成模拟值
    private double getCpuTemperature() {
        double temp = hardware.getSensors().getCpuTemperature();
        if (Double.isNaN(temp) || temp <= 0) {
            return 40 + Math.random() * 20; // 随机模拟温度
        }
        return temp;
    }

    // 获取硬件信息（型号）
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

    // 内部类：硬件信息
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

**亮点说明：**
- ⏱️ 使用 `Timer` 定时采集，采集周期可调节
- 🌡️ 支持多维度监控
- 📝 采集结果实时提供 UI 界面刷新

---

## 3. `logic/ExcelExporter.java` <span style="font-size:0.85em;color:#888;">一键导出</span>

将性能异常数据导出为 Excel 文件，表头清晰。

```java
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
            // 设置表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // 写表头
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
            // 自动适应列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            // 写入Excel文件
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }
}
```

**亮点说明：**
- 📊 使用 Apache POI 导出高质量 Excel 报表
- 📁 导出表头清晰、数据完整，自动适配列宽

---

## 4. `logic/PerformanceData.java` <span style="font-size:0.85em;color:#888;">数据结构</span>

封装单条性能采集数据，自动判定是否异常。

```java
package logic;

import java.time.LocalDateTime;

public class PerformanceData {
    private final LocalDateTime timestamp;   // 采集时间
    private final double cpuUsage;           // CPU使用率
    private final double memoryUsage;        // 内存使用率
    private final double diskUsage;          // 磁盘使用率
    private final double temperature;        // 温度
    private final boolean isAbnormal;        // 是否异常

    public PerformanceData(LocalDateTime timestamp, double cpuUsage,
            double memoryUsage, double diskUsage,
            double temperature) {
        this.timestamp = timestamp;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.diskUsage = diskUsage;
        this.temperature = temperature;
        this.isAbnormal = checkAbnormal(); // 构造时自动判断是否异常
    }

    // 判断是否异常
    private boolean checkAbnormal() {
        final double CPU_THRESHOLD = 90.0;
        final double MEMORY_THRESHOLD = 85.0;
        final double DISK_THRESHOLD = 95.0;
        return cpuUsage > CPU_THRESHOLD ||
                memoryUsage > MEMORY_THRESHOLD ||
                diskUsage > DISK_THRESHOLD;
    }

    // 各字段getter
    public LocalDateTime getTimestamp() { return timestamp; }
    public double getCpuUsage() { return cpuUsage; }
    public double getMemoryUsage() { return memoryUsage; }
    public double getDiskUsage() { return diskUsage; }
    public double getTemperature() { return temperature; }
    public boolean isAbnormal() { return isAbnormal; }
}
```

**亮点说明：**
- 🎯 自动判定异常数据点
- 🧩 封装良好，便于扩展与维护

---

## 5. `ui/BrandLogoManager.java` <span style="font-size:0.85em;color:#888;">品牌Logo智能管理</span>

品牌Logo的加载与缓存。

```java
package ui;

import javafx.scene.image.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BrandLogoManager {
    private static final Map<String, String> BRAND_MAPPING = new HashMap<>(); // 品牌映射
    private static final Map<String, Image> LOGO_CACHE = new HashMap<>();     // logo缓存
    static {
        // 品牌关键字与资源文件名映射
        BRAND_MAPPING.put("intel", "intel");
        BRAND_MAPPING.put("amd", "amd");
        BRAND_MAPPING.put("samsung", "samsung");
        BRAND_MAPPING.put("western digital", "wd");
        BRAND_MAPPING.put("seagate", "seagate");
        BRAND_MAPPING.put("kingston", "kingston");
    }

    // 根据型号自动识别品牌
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

    // 获取品牌logo
    public Image getBrandLogo(String brand) {
        if (LOGO_CACHE.containsKey(brand))
            return LOGO_CACHE.get(brand);
        
        try {
            // 加载资源（打包后也能访问）
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

**亮点说明：**
- 🤖 支持多品牌一键识别和 Logo 加载
- 🚀 内置 Logo 缓存机制，访问更快更省资源

---

## 6. `ui/MainApp.java` <span style="font-size:0.85em;color:#888;">应用入口</span>

JavaFX 应用主入口，负责全局异常捕获和主窗口加载。

```java
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
    
    // 记录异常日志到本地文件
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

**亮点说明：**
- 💡 全局异常日志，保障运行安全
- 🎨 主窗口自适应，界面简洁

---

## 7. `ui/MainController.java` <span style="font-size:0.85em;color:#888;">主界面逻辑</span>

主界面所有控件的事件、数据绑定和动态刷新。

```java
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
    // UI组件
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

    // 监控与界面刷新相关
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
            initUsageChart(); // 初始化折线图
            monitor = new HardwareMonitor();
            logoManager = new BrandLogoManager();
            displayHardwareInfo(); // 展示硬件信息
            monitor.startMonitoring(2); // 2秒采样
            setupUIUpdateTimer(); // 定时刷新UI

            // 监听窗口关闭事件
            Stage stage = (Stage) cpuUsageLabel.getScene().getWindow();
            stage.setOnCloseRequest(this::handleWindowClose);
        } catch (Exception e) {
            e.printStackTrace();
            showError("初始化失败: " + e.getMessage());
        }
    }

    // 展示硬件信息
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

    // 定时刷新UI
    private void setupUIUpdateTimer() {
        uiUpdateTimer = new AnimationTimer() {
            private long lastUpdate = 0;
            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000L) { // 每秒刷新
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

    // 更新UI显示
    public void updateUI(PerformanceData data) {
        cpuUsageLabel.setText(String.format("%.1f%%", data.getCpuUsage()));
        memoryUsageLabel.setText(String.format("%.1f%%", data.getMemoryUsage()));
        diskUsageLabel.setText(String.format("%.1f%%", data.getDiskUsage()));
        temperatureLabel.setText(String.format("%.1f°C", data.getTemperature()));

        // 异常状态高亮
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

    // 导出Excel
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

    // 折线图初始化
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

    // 图表可见性切换
    private void updateChartVisibility() {
        if (cpuSeries != null) cpuSeries.getNode().setVisible(cpuMenuItem.isSelected());
        if (memorySeries != null) memorySeries.getNode().setVisible(memoryMenuItem.isSelected());
        if (diskSeries != null) diskSeries.getNode().setVisible(diskMenuItem.isSelected());
    }

    @FXML
    private void handleHardwareSelection() {
        updateChartVisibility();
    }

    // 清空图表
    @FXML
    private void handleResetChart() {
        if (cpuSeries != null) cpuSeries.getData().clear();
        if (memorySeries != null) memorySeries.getData().clear();
        if (diskSeries != null) diskSeries.getData().clear();
        timeCounter = 0;
    }

    // 关闭窗口
    private void handleWindowClose(WindowEvent event) {
        stopMonitoring();
        Platform.exit();
        System.exit(0);
    }

    // 更新折线图
    private void updateChart(PerformanceData data) {
        if (cpuSeries == null || memorySeries == null || diskSeries == null) return;
        cpuSeries.getData().add(new XYChart.Data<>(timeCounter, data.getCpuUsage()));
        memorySeries.getData().add(new XYChart.Data<>(timeCounter, data.getMemoryUsage()));
        diskSeries.getData().add(new XYChart.Data<>(timeCounter, data.getDiskUsage()));

        // 保持固定长度
        if (cpuSeries.getData().size() > MAX_DATA_POINTS) cpuSeries.getData().remove(0);
        if (memorySeries.getData().size() > MAX_DATA_POINTS) memorySeries.getData().remove(0);
        if (diskSeries.getData().size() > MAX_DATA_POINTS) diskSeries.getData().remove(0);

        // 更新X轴
        if (usageChart.getXAxis() instanceof NumberAxis) {
            NumberAxis xAxis = (NumberAxis) usageChart.getXAxis();
            xAxis.setLowerBound(Math.max(0, timeCounter - MAX_DATA_POINTS));
            xAxis.setUpperBound(timeCounter);
        }

        timeCounter++;
        updateChartColors(data);
    }

    // 根据阈值设置折线颜色
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

**亮点说明：**
- 📈 实时折线图刷新
- 🛡️ 异常状态高亮，极致易用

---

> 如需查看更多源码或博客页面，欢迎访问 [GitHub仓库](https://github.com/LXZ-rgb/Performance-Monitor)
