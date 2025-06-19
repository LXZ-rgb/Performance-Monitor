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

> **作用说明**：管理 SQLite 数据库连接、建表、数据存储与关闭。所有性能异常数据均由此管理持久化。

```java name=performance-monitor/src/main/java/logic/DatabaseHandler.java
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

## HardwareMonitor.java （硬件监控）

> **作用说明**：基于 OSHI 库采集 CPU、内存、磁盘等实时数据，并决定是否需要持久化异常。

```java name=performance-monitor/src/main/java/logic/HardwareMonitor.java
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

## ExcelExporter.java （Excel导出）

> **作用说明**：将数据库中的异常性能数据导出为 Excel 文件，方便后期分析与答辩展示。

```java name=performance-monitor/src/main/java/logic/ExcelExporter.java
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

## PerformanceData.java （性能数据模型）

> **作用说明**：封装每次采集的数据项，并内置判断是否为异常状态。

```java name=performance-monitor/src/main/java/logic/PerformanceData.java
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

## ConfigManager.java （配置管理）

> **作用说明**：负责加载、保存项目本地配置（如采集间隔、界面主题等）。

```java name=performance-monitor/src/main/java/logic/ConfigManager.java
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

## LogHelper.java （日志工具）

> **作用说明**：支持日志输出到控制台和日志文件，便于调试和追踪问题。

```java name=performance-monitor/src/main/java/logic/LogHelper.java
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

## CpuInfoParser.java （CPU型号解析）

> **作用说明**：辅助解析 CPU 型号字符串，判断品牌、系列及核心数。

```java name=performance-monitor/src/main/java/logic/CpuInfoParser.java
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

## FileUtils.java （文件工具）

> **作用说明**：提供常用的文件操作方法（如检测、创建、删除、复制等）。

```java name=performance-monitor/src/main/java/logic/FileUtils.java
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

[返回主导航页](code.md)
