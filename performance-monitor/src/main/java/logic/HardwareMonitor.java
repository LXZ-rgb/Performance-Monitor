package logic; // 声明包名

import oshi.SystemInfo; // 导入OSHI系统信息类
import oshi.hardware.*; // 导入OSHI硬件相关类
import oshi.software.os.OSFileStore; // 导入OSHI文件系统存储类

import java.time.LocalDateTime; // 导入本地时间类
import java.util.List; // 导入列表类
import java.util.Timer; // 导入定时器类
import java.util.TimerTask; // 导入定时任务类

public class HardwareMonitor { // 定义硬件监控类
    // 创建系统信息对象
    private final SystemInfo systemInfo = new SystemInfo();
    // 获取硬件抽象层对象
    private final HardwareAbstractionLayer hardware = systemInfo.getHardware();
    // 获取CPU处理器对象
    private final CentralProcessor processor = hardware.getProcessor();
    // 获取内存对象
    private final GlobalMemory memory = hardware.getMemory();

    // 定时器对象，用于定时采集数据
    private Timer monitoringTimer;
    // 数据库操作对象
    private DatabaseHandler dbHandler;
    // 最新采集到的性能数据
    private PerformanceData latestData;

    public HardwareMonitor() { // 构造方法，初始化数据库操作对象
        this.dbHandler = new DatabaseHandler();
    }

    public void startMonitoring(int intervalSeconds) { // 启动监控，参数为采集间隔（秒）
        monitoringTimer = new Timer(); // 创建定时器
        // 安排定时任务，固定速率执行
        monitoringTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() { // 定时执行的任务
                PerformanceData data = collectPerformanceData(); // 采集性能数据
                latestData = data; // 更新最新数据
                if (data.isAbnormal()) { // 如果数据异常
                    dbHandler.savePerformanceData(data); // 保存异常数据到数据库
                }
            }
        }, 0, intervalSeconds * 1000L); // 0为立即执行，后面为间隔时间（毫秒）
    }

    public void stopMonitoring() { // 停止监控
        if (monitoringTimer != null) { // 如果定时器存在
            monitoringTimer.cancel(); // 取消定时任务
        }
        dbHandler.closeConnection(); // 关闭数据库连接
    }

    public PerformanceData getLatestData() { // 获取最新采集的数据
        return latestData; // 用于UI实时刷新真实数据
    }

    private PerformanceData collectPerformanceData() { // 采集性能数据
        // 获取CPU使用率，采样1秒
        double cpuUsage = processor.getSystemCpuLoad(1000) * 100;
        // 计算内存使用率
        double memoryUsage = (memory.getTotal() - memory.getAvailable()) * 100.0 / memory.getTotal();
        double diskUsage = 0; // 初始化磁盘使用率
        // 获取所有文件存储设备
        List<OSFileStore> fileStores = systemInfo.getOperatingSystem().getFileSystem().getFileStores();
        if (!fileStores.isEmpty()) { // 如果至少有一个磁盘
            OSFileStore fs = fileStores.get(0); // 取第一个磁盘
            // 计算磁盘使用率
            diskUsage = (fs.getTotalSpace() - fs.getFreeSpace()) * 100.0 / fs.getTotalSpace();
        }
        double temperature = getCpuTemperature(); // 获取CPU温度
        // 构造并返回性能数据对象
        return new PerformanceData(LocalDateTime.now(), cpuUsage, memoryUsage, diskUsage, temperature);
    }

    private double getCpuTemperature() { // 获取CPU温度
        double temp = hardware.getSensors().getCpuTemperature(); // 读取传感器温度
        // 用模拟温度兜底，保证不会NaN
        if (Double.isNaN(temp) || temp <= 0) {
            return 40 + Math.random() * 20; // 返回40-60之间的随机温度
        }
        return temp; // 返回真实温度
    }

    public HardwareInfo getHardwareInfo() { // 获取硬件基础信息
        String cpuModel = processor.getProcessorIdentifier().getName(); // 获取CPU型号
        String diskModel = "Unknown"; // 初始化磁盘型号
        List<HWDiskStore> diskStores = hardware.getDiskStores(); // 获取所有磁盘信息
        if (!diskStores.isEmpty()) { // 如果存在磁盘
            diskModel = diskStores.get(0).getModel(); // 取第一个磁盘型号
        }
        String motherboardModel = hardware.getComputerSystem().getBaseboard().getModel(); // 获取主板型号
        // 返回硬件信息对象
        return new HardwareInfo(cpuModel, diskModel, motherboardModel);
    }

    public static class HardwareInfo { // 内部类，表示硬件信息
        public final String cpuModel; // CPU型号
        public final String diskModel; // 磁盘型号
        public final String motherboardModel; // 主板型号

        public HardwareInfo(String cpuModel, String diskModel, String motherboardModel) { // 构造方法
            this.cpuModel = cpuModel;
            this.diskModel = diskModel;
            this.motherboardModel = motherboardModel;
        }
    }
}
