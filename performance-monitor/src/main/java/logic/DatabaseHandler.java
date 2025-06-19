package logic; // 指定包名为logic，便于管理逻辑处理相关的类

import java.sql.*; // 导入JDBC相关包，用于数据库连接和操作
import java.nio.file.*; // 导入NIO文件处理包，用于文件和目录操作

public class DatabaseHandler { // 数据库处理器类，负责性能数据的存储和管理
    private Connection connection; // JDBC数据库连接对象

    public DatabaseHandler() { // 构造方法，初始化数据库连接
        try {
            // 获取用户主目录下的专用应用文件夹（跨平台兼容）
            String userHome = System.getProperty("user.home"); // 获取当前用户主目录
            String appDir = userHome + "/PerformanceMonitor"; // 构建应用数据目录
            Path dbPath = Paths.get(appDir, "performance.db"); // 构建数据库文件完整路径
            
            // 确保目录存在，如不存在则自动创建
            Files.createDirectories(dbPath.getParent());
            
            // 加载SQLite JDBC驱动
            Class.forName("org.sqlite.JDBC");
            // 连接到SQLite数据库（如文件不存在则自动创建）
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            // 创建性能数据表（如果尚未存在）
            createTable();
        } catch (Exception e) {
            System.err.println("数据库连接失败: " + e.getMessage()); // 连接失败时输出错误信息
        }
    }

    // 创建性能数据表，表结构设计与PerformanceData类属性一致
    private void createTable() {
        final String sql = """
                CREATE TABLE IF NOT EXISTS performance_data (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,      -- 主键自增
                    timestamp TEXT NOT NULL,                   -- 时间戳，字符串格式
                    cpu_usage REAL NOT NULL,                   -- CPU使用率
                    memory_usage REAL NOT NULL,                -- 内存使用率
                    disk_usage REAL NOT NULL,                  -- 磁盘使用率
                    temperature REAL NOT NULL                  -- 温度
                )
                """;
        try (Statement stmt = connection.createStatement()) { // 使用try-with-resources自动关闭Statement
            stmt.execute(sql); // 执行建表语句
        } catch (SQLException e) {
            System.err.println("创建表失败: " + e.getMessage()); // 建表失败时输出错误信息
        }
    }

    // 将一条性能监控数据存入数据库
    public void savePerformanceData(PerformanceData data) {
        final String sql = "INSERT INTO performance_data (timestamp, cpu_usage, memory_usage, disk_usage, temperature) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) { // 预编译SQL，防SQL注入
            pstmt.setString(1, data.getTimestamp().toString()); // 设置时间戳参数
            pstmt.setDouble(2, data.getCpuUsage()); // 设置CPU使用率
            pstmt.setDouble(3, data.getMemoryUsage()); // 设置内存使用率
            pstmt.setDouble(4, data.getDiskUsage()); // 设置磁盘使用率
            pstmt.setDouble(5, data.getTemperature()); // 设置温度
            pstmt.executeUpdate(); // 执行插入操作
        } catch (SQLException e) {
            System.err.println("保存数据失败: " + e.getMessage()); // 数据保存失败时输出错误信息
        }
    }

    // 关闭数据库连接，释放资源
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) { // 如果连接存在且未关闭
                connection.close(); // 关闭连接
            }
        } catch (SQLException e) {
            System.err.println("关闭数据库连接失败: " + e.getMessage()); // 关闭失败时输出错误信息
        }
    }
    
    // 静态方法，获取数据库文件的绝对路径
    public static String getDatabasePath() {
        String userHome = System.getProperty("user.home"); // 获取用户主目录
        return Paths.get(userHome, "PerformanceMonitor", "performance.db").toString(); // 返回数据库路径
    }
}
