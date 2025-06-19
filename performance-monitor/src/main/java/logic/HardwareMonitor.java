package logic; // 指定包名为logic，便于管理与硬件监控相关的代码

import java.lang.management.ManagementFactory; // 导入Java管理工厂类，用于获取运行时MXBean
import java.lang.management.OperatingSystemMXBean; // 导入操作系统MXBean，用于监控系统指标
import java.time.LocalDateTime; // 导入时间类，用于记录数据采集时间
import java.util.Random; // 导入随机数类（用于模拟非真实硬件环境）
import java.util.Timer; // 导入定时器
import java.util.TimerTask; // 导入定时任务
import logic.PerformanceData; // 导入性能数据结构

public class HardwareMonitor { // 硬件监视器类，定时采集系统性能数据

    private Timer timer; // 定时任务调度器
    private PerformanceData latestData; // 最近一次采集到的数据
    private boolean running = false; // 监控是否在运行

    // 内部类，保存硬件信息
    public static class HardwareInfo {
        public String cpuModel; // CPU型号
        public String diskModel; // 磁盘型号
        public HardwareInfo(String cpu, String disk) {
            this.cpuModel = cpu;
            this.diskModel = disk;
        }
    }

    // 获取硬件信息（这里只做简单模拟，实际应用需调用系统API或第三方库获取真实数据）
    public HardwareInfo getHardwareInfo() {
        // 实际环境下可用命令行、WMI、JNI等方式获取
        String cpuModel = "Intel(R) Core(TM) i7-8700 CPU"; // 示例CPU型号
        String diskModel = "Samsung SSD 970 EVO"; // 示例磁盘型号
        return new HardwareInfo(cpuModel, diskModel);
    }

    // 启动定时监控
    public void startMonitoring(int intervalSeconds) {
        if (running) return; // 防止重复启动
        timer = new Timer(true); // 设置为守护线程
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                collectPerformanceData(); // 采集并更新性能数据
            }
        }, 0, intervalSeconds * 1000); // 按指定秒数周期执行
        running = true;
    }

    // 停止监控
    public void stopMonitoring() {
        if (timer != null) {
            timer.cancel(); // 取消所有定时任务
            timer = null;
        }
        running = false;
    }

    // 采集一次性能数据（实际部署中应替换为真实数据采集代码）
    private void collectPerformanceData() {
        // 获取操作系统MXBean，可采集部分基础系统信息
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        // 这里只做简单模拟，实际代码应调用系统API（如SIGAR、oshi等）获取真实CPU、内存、磁盘和温度数据
        Random rand = new Random();
        double cpuUsage = 60 + rand.nextDouble() * 40; // 随机模拟60~100之间的CPU使用率
        double memoryUsage = 50 + rand.nextDouble() * 50; // 随机模拟50~100之间的内存使用率
        double diskUsage = 30 + rand.nextDouble() * 70; // 随机模拟30~100之间的磁盘使用率
        double temperature = 40 + rand.nextDouble() * 40; // 随机模拟40~80度

        // 获取当前时间
        LocalDateTime timestamp = LocalDateTime.now();

        // 构造性能数据对象
        latestData = new PerformanceData(timestamp, cpuUsage, memoryUsage, diskUsage, temperature);
    }

    // 获取最近一次采集到的性能数据
    public PerformanceData getLatestData() {
        return latestData;
    }
}
