package logic; // 声明包名

import java.sql.*; // 导入JDBC相关的类
import java.nio.file.*; // 导入文件路径相关的类

public class DatabaseHandler { // 定义数据库操作处理类
    private Connection connection; // 数据库连接对象

    public DatabaseHandler() { // 构造方法，初始化数据库连接
        try {
            // 获取用户主目录路径
            String userHome = System.getProperty("user.home");
            // 拼接应用专用目录路径
            String appDir = userHome + "/PerformanceMonitor";
            // 构造数据库文件完整路径
            Path dbPath = Paths.get(appDir, "performance.db");
            
            // 确保数据库所在目录存在，如不存在则创建
            Files.createDirectories(dbPath.getParent());
            
            // 加载SQLite JDBC驱动
            Class.forName("org.sqlite.JDBC");
            // 建立到SQLite数据库的连接
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            // 创建数据库表（如不存在）
            createTable();
        } catch (Exception e) {
            // 捕获异常并打印错误信息
            System.err.println("数据库连接失败: " + e.getMessage());
        }
    }

    private void createTable() { // 创建表的方法
        // 定义建表SQL语句
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
        try (Statement stmt = connection.createStatement()) { // 创建Statement对象
            stmt.execute(sql); // 执行建表语句
        } catch (SQLException e) {
            // 捕获异常并打印错误信息
            System.err.println("创建表失败: " + e.getMessage());
        }
    }

    public void savePerformanceData(PerformanceData data) { // 保存性能数据方法
        // 定义插入数据的SQL语句
        final String sql = "INSERT INTO performance_data (timestamp, cpu_usage, memory_usage, disk_usage, temperature) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) { // 预编译SQL语句
            pstmt.setString(1, data.getTimestamp().toString()); // 设置时间戳参数
            pstmt.setDouble(2, data.getCpuUsage()); // 设置CPU使用率
            pstmt.setDouble(3, data.getMemoryUsage()); // 设置内存使用率
            pstmt.setDouble(4, data.getDiskUsage()); // 设置磁盘使用率
            pstmt.setDouble(5, data.getTemperature()); // 设置温度
            pstmt.executeUpdate(); // 执行插入操作
        } catch (SQLException e) {
            // 捕获异常并打印错误信息
            System.err.println("保存数据失败: " + e.getMessage());
        }
    }

    public void closeConnection() { // 关闭数据库连接方法
        try {
            // 如果连接对象不为空且未关闭，则关闭连接
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            // 捕获异常并打印错误信息
            System.err.println("关闭数据库连接失败: " + e.getMessage());
        }
    }
    
    public static String getDatabasePath() { // 获取数据库文件路径的静态方法
        // 获取用户主目录路径
        String userHome = System.getProperty("user.home");
        // 拼接数据库文件完整路径并返回
        return Paths.get(userHome, "PerformanceMonitor", "performance.db").toString();
    }
}
