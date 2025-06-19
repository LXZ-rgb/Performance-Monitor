package logic; // 声明该类属于logic包

import java.util.List; // 导入List
import java.time.LocalDateTime; // 导入时间

/**
 * 历史性能数据趋势分析工具类
 */
public class HistoryAnalyzer { // 历史分析类
    private final List<PerformanceData> history; // 性能历史数据

    public HistoryAnalyzer(List<PerformanceData> history) { // 构造方法
        this.history = history; // 保存传入的历史数据
    }

    public boolean isCpuLoadRising() { // 检查CPU负载是否持续升高
        if (history.size() < 3)
            return false; // 数据不足返回false
        double prev = history.get(0).getCpuUsage();
        for (int i = 1; i < history.size(); i++) {
            double cur = history.get(i).getCpuUsage();
            if (cur < prev)
                return false; // 有下降则不是持续升高
            prev = cur;
        }
        return true;
    }

    public double getMaxDropInMemory() { // 计算内存使用率最大下降幅度
        double maxDrop = 0.0;
        for (int i = 1; i < history.size(); i++) {
            double drop = history.get(i - 1).getMemoryUsage() - history.get(i).getMemoryUsage();
            if (drop > maxDrop)
                maxDrop = drop;
        }
        return maxDrop;
    }

    public int countOverThresholds(double cpu, double mem, double disk, double temp) { // 统计多项指标超阈值总次数
        int count = 0;
        for (PerformanceData d : history) {
            if (d.getCpuUsage() > cpu)
                count++;
            if (d.getMemoryUsage() > mem)
                count++;
            if (d.getDiskUsage() > disk)
                count++;
            if (d.getTemperature() > temp)
                count++;
        }
        return count;
    }

    public PerformanceData getPeakCpuData() { // 返回CPU使用率最高的数据
        if (history.isEmpty())
            return null;
        PerformanceData peak = history.get(0);
        for (PerformanceData d : history) {
            if (d.getCpuUsage() > peak.getCpuUsage())
                peak = d;
        }
        return peak;
    }

    public LocalDateTime getFirstAbnormalTime(double cpuT, double memT, double diskT, double tempT) { // 返回首次异常时间
        for (PerformanceData d : history) {
            if (d.getCpuUsage() > cpuT || d.getMemoryUsage() > memT || d.getDiskUsage() > diskT
                    || d.getTemperature() > tempT) {
                return d.getTimestamp();
            }
        }
        return null;
    }
}