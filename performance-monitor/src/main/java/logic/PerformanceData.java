package logic; // 指定包名为logic，便于管理数据结构相关代码

import java.time.LocalDateTime; // 导入时间类，用于记录数据采集时间

public class PerformanceData { // 性能数据结构类，封装采集到的各项监控指标

    private final LocalDateTime timestamp; // 采集时间
    private final double cpuUsage;         // CPU使用率（百分比）
    private final double memoryUsage;      // 内存使用率（百分比）
    private final double diskUsage;        // 磁盘使用率（百分比）
    private final double temperature;      // 温度（摄氏度）

    // 性能异常判定阈值，需与UI和数据导出等模块保持一致
    private static final double CPU_THRESHOLD = 90.0;
    private static final double MEMORY_THRESHOLD = 85.0;
    private static final double DISK_THRESHOLD = 95.0;

    // 构造方法，初始化所有字段
    public PerformanceData(LocalDateTime timestamp, double cpuUsage, double memoryUsage, double diskUsage, double temperature) {
        this.timestamp = timestamp;
        this.cpuUsage = cpuUsage;
        this.memoryUsage = memoryUsage;
        this.diskUsage = diskUsage;
        this.temperature = temperature;
    }

    // 获取采集时间
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // 获取CPU使用率
    public double getCpuUsage() {
        return cpuUsage;
    }

    // 获取内存使用率
    public double getMemoryUsage() {
        return memoryUsage;
    }

    // 获取磁盘使用率
    public double getDiskUsage() {
        return diskUsage;
    }

    // 获取温度
    public double getTemperature() {
        return temperature;
    }

    // 判断当前数据是否异常（任意一项超过阈值即为异常）
    public boolean isAbnormal() {
        return cpuUsage > CPU_THRESHOLD || memoryUsage > MEMORY_THRESHOLD || diskUsage > DISK_THRESHOLD;
    }
}
