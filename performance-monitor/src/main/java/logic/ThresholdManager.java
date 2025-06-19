package logic; // 声明该类属于logic包

/**
 * 性能阈值管理类，支持动态调整阈值
 */
public class ThresholdManager { // 阈值管理器定义
    private double cpuThreshold; // CPU阈值
    private double memoryThreshold; // 内存阈值
    private double diskThreshold; // 磁盘阈值
    private double temperatureThreshold; // 温度阈值

    public ThresholdManager() { // 构造方法，默认阈值
        cpuThreshold = 90.0; // 默认CPU
        memoryThreshold = 85.0; // 默认内存
        diskThreshold = 95.0; // 默认磁盘
        temperatureThreshold = 80.0; // 默认温度
    }

    public double getCpuThreshold() {
        return cpuThreshold;
    } // 获取CPU阈值

    public void setCpuThreshold(double t) {
        cpuThreshold = t;
    } // 设置CPU阈值

    public double getMemoryThreshold() {
        return memoryThreshold;
    } // 获取内存阈值

    public void setMemoryThreshold(double t) {
        memoryThreshold = t;
    } // 设置内存阈值

    public double getDiskThreshold() {
        return diskThreshold;
    } // 获取磁盘阈值

    public void setDiskThreshold(double t) {
        diskThreshold = t;
    } // 设置磁盘阈值

    public double getTemperatureThreshold() {
        return temperatureThreshold;
    } // 获取温度阈值

    public void setTemperatureThreshold(double t) {
        temperatureThreshold = t;
    } // 设置温度阈值

    public boolean isCpuAbnormal(double val) {
        return val > cpuThreshold;
    } // 判断CPU是否异常

    public boolean isMemoryAbnormal(double val) {
        return val > memoryThreshold;
    } // 判断内存是否异常

    public boolean isDiskAbnormal(double val) {
        return val > diskThreshold;
    } // 判断磁盘是否异常

    public boolean isTemperatureAbnormal(double val) {
        return val > temperatureThreshold;
    } // 判断温度是否异常
}