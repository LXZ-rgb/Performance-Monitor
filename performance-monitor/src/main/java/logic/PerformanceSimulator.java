package logic; // 声明该类属于logic包

import java.time.LocalDateTime; // 导入时间类
import java.util.Random; // 导入随机数类

/**
 * 性能数据模拟器，用于生成随机性能数据，便于测试
 */
public class PerformanceSimulator { // 性能模拟器定义
    private final Random random; // 随机数生成器

    public PerformanceSimulator() { // 构造方法
        random = new Random(); // 初始化随机数生成器
    }

    public PerformanceData generateRandomData() { // 生成一条随机性能数据
        LocalDateTime now = LocalDateTime.now(); // 当前时间
        double cpu = 20 + random.nextDouble() * 80; // 随机CPU 20-100%
        double mem = 30 + random.nextDouble() * 60; // 随机内存30-90%
        double disk = 10 + random.nextDouble() * 85; // 随机磁盘10-95%
        double temp = 35 + random.nextDouble() * 40; // 随机温度35-75度
        return new PerformanceData(now, cpu, mem, disk, temp); // 构造数据对象
    }

    public PerformanceData generateCustomData(double cpu, double mem, double disk, double temp) { // 生成指定数据
        LocalDateTime now = LocalDateTime.now(); // 当前时间
        return new PerformanceData(now, cpu, mem, disk, temp); // 构造数据对象
    }
}