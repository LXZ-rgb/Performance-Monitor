package logic; // 声明该类属于logic包

/**
 * CPU型号字符串解析工具类
 */
public class CpuInfoParser { // CPU信息解析器
    public static String getBrand(String cpuModel) { // 提取品牌
        if (cpuModel == null)
            return "";
        String s = cpuModel.toLowerCase();
        if (s.contains("intel"))
            return "Intel";
        if (s.contains("amd"))
            return "AMD";
        if (s.contains("apple"))
            return "Apple";
        return "Unknown";
    }

    public static String getSeries(String cpuModel) { // 提取系列
        if (cpuModel == null)
            return "";
        if (cpuModel.contains("i7"))
            return "i7";
        if (cpuModel.contains("i5"))
            return "i5";
        if (cpuModel.contains("i3"))
            return "i3";
        if (cpuModel.contains("Ryzen"))
            return "Ryzen";
        return "Unknown";
    }

    public static int getCoreCount(String cpuModel) { // 根据型号估算核心数
        if (cpuModel == null)
            return 0;
        if (cpuModel.contains("i7"))
            return 8;
        if (cpuModel.contains("i5"))
            return 6;
        if (cpuModel.contains("i3"))
            return 4;
        if (cpuModel.contains("Ryzen 9"))
            return 12;
        if (cpuModel.contains("Ryzen 7"))
            return 8;
        if (cpuModel.contains("Ryzen 5"))
            return 6;
        return 4;
    }
}