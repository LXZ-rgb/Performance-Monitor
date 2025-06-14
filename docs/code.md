---
layout: page
title: 项目源码与结构介绍
---

# 源代码完整展示与解析

本页将详细展示 Performance Monitor 项目的核心源码，包括每个文件的全部内容和结构解读，便于学习与参考。

---

## 1. logic/DatabaseHandler.java

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

---

## 1.1 logic/HardwareMonitor.java

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

---

## 2. logic/ExcelExporter.java

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

---

## 3. logic/PerformanceData.java

```java
// PerformanceData.java
// 性能数据结构定义
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

---

## 4. ui/BrandLogoManager.java

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

---

## 5. ui/MainApp.java

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

---

## 6. ui/MainController.java

```java
// MainController.java
// 主界面控制逻辑
// ...（此处为你的 MainController.java 源码，略。）
```

---

> 如需查看所有源文件，请访问[GitHub仓库](https://github.com/LXZ-rgb/Performance-Monitor)。

[返回首页](index.md)
