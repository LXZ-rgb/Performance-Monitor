package logic; // 声明包名

import java.time.LocalDateTime; // 导入本地日期时间类

public class PerformanceData { // 定义性能数据类
    private final LocalDateTime timestamp; // 记录采集数据的时间戳
    private final double cpuUsage; // CPU使用率
    private final double memoryUsage; // 内存使用率
    private final double diskUsage; // 磁盘使用率
    private final double temperature; // 温度
    private final boolean isAbnormal; // 是否为异常数据

    public PerformanceData(LocalDateTime timestamp, double cpuUsage,
            double memoryUsage, double diskUsage,
            double temperature) { // 构造方法，初始化所有字段
        this.timestamp = timestamp; // 设置时间戳
        this.cpuUsage = cpuUsage; // 设置CPU使用率
        this.memoryUsage = memoryUsage; // 设置内存使用率
        this.diskUsage = diskUsage; // 设置磁盘使用率
        this.temperature = temperature; // 设置温度
        this.isAbnormal = checkAbnormal(); // 检查是否异常并赋值
    }

    private boolean checkAbnormal() { // 判断当前数据是否异常
        final double CPU_THRESHOLD = 90.0; // CPU使用率阈值
        final double MEMORY_THRESHOLD = 85.0; // 内存使用率阈值
        final double DISK_THRESHOLD = 95.0; // 磁盘使用率阈值
        // 只要有一项超过阈值即判定为异常
        return cpuUsage > CPU_THRESHOLD ||
                memoryUsage > MEMORY_THRESHOLD ||
                diskUsage > DISK_THRESHOLD;
    }

    public LocalDateTime getTimestamp() { // 获取时间戳
        return timestamp;
    }

    public double getCpuUsage() { // 获取CPU使用率
        return cpuUsage;
    }

    public double getMemoryUsage() { // 获取内存使用率
        return memoryUsage;
    }

    public double getDiskUsage() { // 获取磁盘使用率
        return diskUsage;
    }

    public double getTemperature() { // 获取温度
        return temperature;
    }

    public boolean isAbnormal() { // 判断是否为异常数据
        return isAbnormal;
    }
}
