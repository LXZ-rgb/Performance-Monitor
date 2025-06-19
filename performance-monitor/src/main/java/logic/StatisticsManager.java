package logic; // 声明该类属于logic包，便于分层管理

import java.util.ArrayList; // 导入ArrayList，用于存储动态性能数据列表
import java.util.List; // 导入List接口，定义数据集合
import java.time.LocalDateTime; // 导入时间类，用于处理时间戳

public class StatisticsManager { // 定义性能数据统计管理类
    private final List<PerformanceData> dataList; // 用于存储性能数据的列表

    public StatisticsManager() {
        dataList = new ArrayList<>();
    } // 构造方法，初始化数据列表

    public void addData(PerformanceData data) {
        dataList.add(data);
    } // 添加一条性能数据

    public List<PerformanceData> getAllData() {
        return new ArrayList<>(dataList);
    } // 返回数据列表的副本

    public int getCount() {
        return dataList.size();
    } // 返回当前数据数量

    public void clearData() {
        dataList.clear();
    } // 清空所有已采集的数据

    public double getAverageCpuUsage() { // 计算CPU使用率平均值
        if (dataList.isEmpty())
            return 0.0; // 如果没有数据则返回0
        double sum = 0.0; // 用于累加CPU使用率
        for (PerformanceData d : dataList) {
            sum += d.getCpuUsage();
        } // 遍历所有数据累加CPU
        return sum / dataList.size(); // 计算平均值
    }

    public double getAverageMemoryUsage() { // 计算内存使用率平均值
        if (dataList.isEmpty())
            return 0.0; // 没有数据返回0
        double sum = 0.0; // 累加变量
        for (PerformanceData d : dataList) {
            sum += d.getMemoryUsage();
        } // 累加内存
        return sum / dataList.size(); // 计算平均值
    }

    public double getAverageDiskUsage() { // 计算磁盘使用率平均值
        if (dataList.isEmpty())
            return 0.0; // 没有数据返回0
        double sum = 0.0; // 累加变量
        for (PerformanceData d : dataList) {
            sum += d.getDiskUsage();
        } // 累加磁盘
        return sum / dataList.size(); // 计算平均值
    }

    public double getAverageTemperature() { // 计算温度平均值
        if (dataList.isEmpty())
            return 0.0; // 没有数据返回0
        double sum = 0.0; // 累加变量
        for (PerformanceData d : dataList) {
            sum += d.getTemperature();
        } // 累加温度
        return sum / dataList.size(); // 计算平均值
    }

    public double getMaxCpuUsage() { // 获取最大CPU使用率
        double max = Double.MIN_VALUE; // 初始设为最小
        for (PerformanceData d : dataList) { // 遍历所有数据
            if (d.getCpuUsage() > max)
                max = d.getCpuUsage(); // 若更大则更新
        }
        return dataList.isEmpty() ? 0.0 : max; // 没有数据返回0
    }

    public double getMinCpuUsage() { // 获取最小CPU使用率
        double min = Double.MAX_VALUE; // 初始设为最大
        for (PerformanceData d : dataList) { // 遍历所有数据
            if (d.getCpuUsage() < min)
                min = d.getCpuUsage(); // 若更小则更新
        }
        return dataList.isEmpty() ? 0.0 : min; // 没有数据返回0
    }

    public double getMaxMemoryUsage() { // 获取最大内存使用率
        double max = Double.MIN_VALUE; // 初始设为最小
        for (PerformanceData d : dataList) { // 遍历所有数据
            if (d.getMemoryUsage() > max)
                max = d.getMemoryUsage(); // 若更大则更新
        }
        return dataList.isEmpty() ? 0.0 : max; // 没有数据返回0
    }

    public double getMinMemoryUsage() { // 获取最小内存使用率
        double min = Double.MAX_VALUE; // 初始设为最大
        for (PerformanceData d : dataList) { // 遍历所有数据
            if (d.getMemoryUsage() < min)
                min = d.getMemoryUsage(); // 若更小则更新
        }
        return dataList.isEmpty() ? 0.0 : min; // 没有数据返回0
    }

    public double getMaxDiskUsage() { // 获取最大磁盘使用率
        double max = Double.MIN_VALUE; // 初始设为最小
        for (PerformanceData d : dataList) { // 遍历所有数据
            if (d.getDiskUsage() > max)
                max = d.getDiskUsage(); // 若更大则更新
        }
        return dataList.isEmpty() ? 0.0 : max; // 没有数据返回0
    }

    public double getMinDiskUsage() { // 获取最小磁盘使用率
        double min = Double.MAX_VALUE; // 初始设为最大
        for (PerformanceData d : dataList) { // 遍历所有数据
            if (d.getDiskUsage() < min)
                min = d.getDiskUsage(); // 若更小则更新
        }
        return dataList.isEmpty() ? 0.0 : min; // 没有数据返回0
    }

    public double getMaxTemperature() { // 获取最大温度
        double max = Double.MIN_VALUE; // 初始设为最小
        for (PerformanceData d : dataList) { // 遍历所有数据
            if (d.getTemperature() > max)
                max = d.getTemperature(); // 若更大则更新
        }
        return dataList.isEmpty() ? 0.0 : max; // 没有数据返回0
    }

    public double getMinTemperature() { // 获取最小温度
        double min = Double.MAX_VALUE; // 初始设为最大
        for (PerformanceData d : dataList) { // 遍历所有数据
            if (d.getTemperature() < min)
                min = d.getTemperature(); // 若更小则更新
        }
        return dataList.isEmpty() ? 0.0 : min; // 没有数据返回0
    }

    public int getAbnormalCount() { // 统计异常数据条数
        int count = 0; // 计数变量
        for (PerformanceData d : dataList) { // 遍历所有数据
            if (d.isAbnormal())
                count++; // 若为异常则累加
        }
        return count; // 返回异常条数
    }

    public PerformanceData getLatestData() { // 获取最新一条数据
        if (dataList.isEmpty())
            return null; // 没有数据返回null
        return dataList.get(dataList.size() - 1); // 返回最后一条
    }

    public List<PerformanceData> getDataBetween(LocalDateTime from, LocalDateTime to) { // 获取指定时间段的数据
        List<PerformanceData> result = new ArrayList<>(); // 新建结果列表
        for (PerformanceData d : dataList) { // 遍历所有数据
            LocalDateTime ts = d.getTimestamp(); // 获取时间戳
            boolean afterFrom = (from == null || !ts.isBefore(from)); // 判断是否在起始时间之后
            boolean beforeTo = (to == null || !ts.isAfter(to)); // 判断是否在结束时间之前
            if (afterFrom && beforeTo)
                result.add(d); // 满足条件则加入结果
        }
        return result; // 返回筛选结果
    }

    public double getAverageCpuUsageBetween(LocalDateTime from, LocalDateTime to) { // 计算指定时间段内CPU平均值
        List<PerformanceData> subset = getDataBetween(from, to); // 获取子集
        if (subset.isEmpty())
            return 0.0; // 没有数据返回0
        double sum = 0.0; // 累加变量
        for (PerformanceData d : subset) {
            sum += d.getCpuUsage();
        } // 累加
        return sum / subset.size(); // 计算平均值
    }

    public double getCpuUsageStdDev() { // 计算CPU使用率标准差
        int n = dataList.size(); // 获取数据数量
        if (n <= 1)
            return 0.0; // 数据量不足返回0
        double avg = getAverageCpuUsage(); // 获取平均值
        double sumSq = 0.0; // 方差累加
        for (PerformanceData d : dataList) { // 遍历每个数据
            double diff = d.getCpuUsage() - avg; // 差值
            sumSq += diff * diff; // 差值平方累加
        }
        return Math.sqrt(sumSq / (n - 1)); // 返回标准差
    }

    // 继续添加更多统计方法以丰富功能和代码行数
    public double getMemoryUsageStdDev() { // 计算内存使用率标准差
        int n = dataList.size();
        if (n <= 1)
            return 0.0;
        double avg = getAverageMemoryUsage();
        double sumSq = 0.0;
        for (PerformanceData d : dataList) {
            double diff = d.getMemoryUsage() - avg;
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq / (n - 1));
    }

    public double getDiskUsageStdDev() { // 计算磁盘使用率标准差
        int n = dataList.size();
        if (n <= 1)
            return 0.0;
        double avg = getAverageDiskUsage();
        double sumSq = 0.0;
        for (PerformanceData d : dataList) {
            double diff = d.getDiskUsage() - avg;
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq / (n - 1));
    }

    public double getTemperatureStdDev() { // 计算温度标准差
        int n = dataList.size();
        if (n <= 1)
            return 0.0;
        double avg = getAverageTemperature();
        double sumSq = 0.0;
        for (PerformanceData d : dataList) {
            double diff = d.getTemperature() - avg;
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq / (n - 1));
    }

    public int countCpuOver(double threshold) { // 统计CPU超过阈值次数
        int count = 0;
        for (PerformanceData d : dataList) {
            if (d.getCpuUsage() > threshold)
                count++;
        }
        return count;
    }

    public int countMemoryOver(double threshold) { // 统计内存超过阈值次数
        int count = 0;
        for (PerformanceData d : dataList) {
            if (d.getMemoryUsage() > threshold)
                count++;
        }
        return count;
    }

    public int countDiskOver(double threshold) { // 统计磁盘超过阈值次数
        int count = 0;
        for (PerformanceData d : dataList) {
            if (d.getDiskUsage() > threshold)
                count++;
        }
        return count;
    }

    public int countTemperatureOver(double threshold) { // 统计温度超过阈值次数
        int count = 0;
        for (PerformanceData d : dataList) {
            if (d.getTemperature() > threshold)
                count++;
        }
        return count;
    }

}