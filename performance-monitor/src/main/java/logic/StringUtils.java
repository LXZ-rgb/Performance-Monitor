package logic; // 声明当前类属于logic包

/**
 * 字符串工具类，提供常用字符串处理方法
 */
public class StringUtils { // 工具类定义

    public static boolean isNullOrEmpty(String s) { // 判断字符串是否为null或空
        return s == null || s.isEmpty(); // 返回判断结果
    }

    public static String trim(String s) { // 去除字符串首尾空格
        if (s == null)
            return null; // null返回null
        return s.trim(); // 返回去空格后的字符串
    }

    public static boolean equalsIgnoreCase(String a, String b) { // 忽略大小写比较两个字符串
        if (a == null)
            return b == null; // a为null时只与b为null时相等
        return a.equalsIgnoreCase(b); // 否则用内置方法比较
    }

    public static String join(String[] arr, String sep) { // 用分隔符拼接字符串数组
        if (arr == null || arr.length == 0)
            return ""; // 空数组返回空串
        StringBuilder sb = new StringBuilder(); // 用于拼接
        for (int i = 0; i < arr.length; i++) { // 遍历数组
            sb.append(arr[i]); // 添加元素
            if (i < arr.length - 1)
                sb.append(sep); // 不是最后一个则加分隔符
        }
        return sb.toString(); // 返回拼接结果
    }

    public static String repeat(String s, int times) { // 重复字符串多次
        if (s == null || times <= 0)
            return ""; // 特殊情况返回空串
        StringBuilder sb = new StringBuilder(); // 用于拼接
        for (int i = 0; i < times; i++) { // 重复次数循环
            sb.append(s); // 添加字符串
        }
        return sb.toString(); // 返回结果
    }

    public static boolean containsIgnoreCase(String src, String part) { // 判断是否包含（忽略大小写）
        if (src == null || part == null)
            return false; // 有null直接false
        return src.toLowerCase().contains(part.toLowerCase()); // 转小写后判断
    }

    public static String substringSafe(String s, int start, int end) { // 安全截取字符串
        if (s == null)
            return null; // null返回null
        int len = s.length(); // 获取长度
        if (start < 0)
            start = 0; // 开始不能小于0
        if (end > len)
            end = len; // 结束不能大于长度
        if (start >= end)
            return ""; // 开始大于等于结束返回空串
        return s.substring(start, end); // 返回截取结果
    }

    public static String reverse(String s) { // 反转字符串
        if (s == null)
            return null; // null直接返回
        return new StringBuilder(s).reverse().toString(); // 用StringBuilder反转
    }
}