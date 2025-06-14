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