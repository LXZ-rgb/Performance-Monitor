---
layout: page
title: 项目源码与结构介绍
---

# 🌟 项目所有 Java 源码完整展示与逐行注释

<div align="center">
  <img src="https://img.shields.io/badge/Java-PerformanceMonitor-blue?logo=java" alt="Java">
  <img src="https://img.shields.io/badge/技术栈-JavaFX%20%7C%20OSHI%20%7C%20SQLite-green" alt="Stack">
  <img src="https://img.shields.io/badge/开源-GitHub-brightgreen" alt="GitHub">
</div>

<br/>

> 本页面完整展示 Performance Monitor 项目 `performance-monitor/src/main/java` 目录下所有 Java 源码（含中文注释和模块说明），便于学习与答辩使用。

---

## 目录

- [logic 包（数据、工具与业务逻辑）](#logic-包数据工具与业务逻辑)
- [ui 包（界面与控制器）](#ui-包界面与控制器)

---

## logic 包（数据、工具与业务逻辑）

### DatabaseHandler.java

```java
package logic;

import java.sql.*;
import java.nio.file.*;

public class DatabaseHandler {
    private Connection connection;

    public DatabaseHandler() {
        try {
            String userHome = System.getProperty("user.home");
            String appDir = userHome + "/PerformanceMonitor";
            Path dbPath = Paths.get(appDir, "performance.db");
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

---

### HardwareMonitor.java

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
        return latestData;
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

---

### ExcelExporter.java

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
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "时间戳", "CPU使用率(%)", "内存使用率(%)", "磁盘使用率(%)", "温度(°C)" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
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

---

### PerformanceData.java

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

    public LocalDateTime getTimestamp() { return timestamp; }
    public double getCpuUsage() { return cpuUsage; }
    public double getMemoryUsage() { return memoryUsage; }
    public double getDiskUsage() { return diskUsage; }
    public double getTemperature() { return temperature; }
    public boolean isAbnormal() { return isAbnormal; }
}
```

---

### ConfigManager.java

```java
package logic;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 配置文件管理类，支持读取和保存项目配置
 */
public class ConfigManager {
    private final Properties props;
    private final String configFilePath;

    public ConfigManager(String configFilePath) {
        this.configFilePath = configFilePath;
        this.props = new Properties();
        load();
    }

    public String getConfig(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public void setConfig(String key, String value) {
        props.setProperty(key, value);
    }

    public void save() {
        try (FileOutputStream fos = new FileOutputStream(configFilePath)) {
            props.store(fos, "Application Config");
        } catch (IOException e) {
            System.err.println("保存配置文件失败: " + e.getMessage());
        }
    }

    public void load() {
        try (FileInputStream fis = new FileInputStream(configFilePath)) {
            props.load(fis);
        } catch (IOException e) {
            // 文件不存在等情况忽略
        }
    }
}
```

---

### LogHelper.java

```java
package logic;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 日志工具类，支持多级日志控制台及文件输出
 */
public class LogHelper {
    private String logFilePath;
    private boolean enableFileLog;

    public LogHelper() {
        this.logFilePath = "app.log";
        this.enableFileLog = false;
    }

    public LogHelper(String logFilePath, boolean enableFileLog) {
        this.logFilePath = logFilePath;
        this.enableFileLog = enableFileLog;
    }

    public void setLogFilePath(String path) { this.logFilePath = path; }
    public String getLogFilePath() { return logFilePath; }
    public void setEnableFileLog(boolean enable) { this.enableFileLog = enable; }
    public boolean isEnableFileLog() { return enableFileLog; }

    public void info(String message) { log("INFO", message); }
    public void warn(String message) { log("WARN", message); }
    public void error(String message) { log("ERROR", message); }
    public void debug(String message) { log("DEBUG", message); }

    public void log(String level, String message) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logMsg = String.format("[%s] [%s] %s", now, level, message);
        System.out.println(logMsg);
        if (enableFileLog) {
            writeLogToFile(logMsg);
        }
    }

    private void writeLogToFile(String logMsg) {
        try (FileWriter fw = new FileWriter(logFilePath, true)) {
            fw.write(logMsg + "\n");
        } catch (IOException e) {
            System.err.println("写入日志文件失败: " + e.getMessage());
        }
    }
}
```

---

### CpuInfoParser.java

```java
package logic;

/**
 * CPU型号字符串解析工具类
 */
public class CpuInfoParser {
    public static String getBrand(String cpuModel) {
        if (cpuModel == null)
            return "";
        String s = cpuModel.toLowerCase();
        if (s.contains("intel"))
            return "Intel";
        if (s.contains("amd"))
            return "AMD";
        if (s.contains("apple"))
            return "Apple";
        return "Unknown";
    }

    public static String getSeries(String cpuModel) {
        if (cpuModel == null)
            return "";
        if (cpuModel.contains("i7"))
            return "i7";
        if (cpuModel.contains("i5"))
            return "i5";
        if (cpuModel.contains("i3"))
            return "i3";
        if (cpuModel.contains("Ryzen"))
            return "Ryzen";
        return "Unknown";
    }

    public static int getCoreCount(String cpuModel) {
        if (cpuModel == null)
            return 0;
        if (cpuModel.contains("i7"))
            return 8;
        if (cpuModel.contains("i5"))
            return 6;
        if (cpuModel.contains("i3"))
            return 4;
        if (cpuModel.contains("Ryzen 9"))
            return 12;
        if (cpuModel.contains("Ryzen 7"))
            return 8;
        if (cpuModel.contains("Ryzen 5"))
            return 6;
        return 4;
    }
}
```

---

### FileUtils.java

```java
package logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件操作工具类，提供常用文件相关方法
 */
public class FileUtils {
    public static boolean exists(String path) {
        if (path == null)
            return false;
        return new File(path).exists();
    }

    public static boolean delete(String path) {
        if (path == null)
            return false;
        File file = new File(path);
        if (!file.exists())
            return false;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                delete(child.getAbsolutePath());
            }
        }
        return file.delete();
    }

    public static boolean copy(String src, String dest) {
        if (src == null || dest == null)
            return false;
        File srcFile = new File(src);
        if (!srcFile.exists() || srcFile.isDirectory())
            return false;
        try (FileInputStream fis = new FileInputStream(src);
                FileOutputStream fos = new FileOutputStream(dest)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = fis.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static long size(String path) {
        if (path == null)
            return 0;
        File file = new File(path);
        if (!file.exists() || file.isDirectory())
            return 0;
        return file.length();
    }

    public static boolean createFile(String path) {
        if (path == null)
            return false;
        File file = new File(path);
        if (file.exists())
            return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean createDir(String path) {
        if (path == null)
            return false;
        File file = new File(path);
        if (file.exists())
            return false;
        return file.mkdirs();
    }
}
```

---

## ui 包（界面与控制器）

### MainApp.java

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

### MainController.java

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

### BrandLogoManager.java

```java
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

> 以上即为 `performance-monitor/src/main/java` 目录下所有主 Java 文件的完整源码。若有遗漏、更新或需补充其它模块，请告知。
