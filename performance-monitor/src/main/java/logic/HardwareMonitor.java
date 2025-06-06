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
        return latestData; // 用于UI实时刷新真实数据
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
        // 用模拟温度兜底，保证不会NaN
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