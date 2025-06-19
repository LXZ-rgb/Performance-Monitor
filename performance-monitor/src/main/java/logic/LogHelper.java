package logic; // 声明当前类属于logic包，便于逻辑功能统一管理

import java.io.FileWriter; // 导入FileWriter类，用于文件写入
import java.io.IOException; // 导入IOException，处理IO异常
import java.time.LocalDateTime; // 导入LocalDateTime，记录日志时间
import java.time.format.DateTimeFormatter; // 导入DateTimeFormatter，格式化时间字符串

/**
 * 日志工具类，支持多级日志控制台及文件输出
 */
public class LogHelper { // 日志工具类定义
    private String logFilePath; // 日志文件路径
    private boolean enableFileLog; // 是否启用文件日志

    public LogHelper() { // 默认构造函数
        this.logFilePath = "app.log"; // 默认日志文件名
        this.enableFileLog = false; // 默认不写入文件
    }

    public LogHelper(String logFilePath, boolean enableFileLog) { // 构造函数，指定文件名和是否输出文件
        this.logFilePath = logFilePath; // 设置日志文件名
        this.enableFileLog = enableFileLog; // 设置是否启用文件日志
    }

    public void setLogFilePath(String path) {
        this.logFilePath = path;
    } // 设置日志文件路径

    public String getLogFilePath() {
        return logFilePath;
    } // 获取日志文件路径

    public void setEnableFileLog(boolean enable) {
        this.enableFileLog = enable;
    } // 设置是否记录到文件

    public boolean isEnableFileLog() {
        return enableFileLog;
    } // 获取是否开启文件日志

    public void info(String message) { // 输出INFO级别日志
        log("INFO", message); // 调用主日志方法
    }

    public void warn(String message) { // 输出WARN级别日志
        log("WARN", message); // 调用主日志方法
    }

    public void error(String message) { // 输出ERROR级别日志
        log("ERROR", message); // 调用主日志方法
    }

    public void debug(String message) { // 输出DEBUG级别日志
        log("DEBUG", message); // 调用主日志方法
    }

    public void log(String level, String message) { // 实际日志输出方法
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")); // 获取当前时间字符串
        String logMsg = String.format("[%s] [%s] %s", now, level, message); // 组装日志字符串
        System.out.println(logMsg); // 控制台打印日志
        if (enableFileLog) { // 若启用文件日志
            writeLogToFile(logMsg); // 写入文件
        }
    }

    private void writeLogToFile(String logMsg) { // 写入日志到文件
        try (FileWriter fw = new FileWriter(logFilePath, true)) { // 以追加模式打开文件
            fw.write(logMsg + "\n"); // 写入日志并换行
        } catch (IOException e) { // 捕获写入异常
            System.err.println("写入日志文件失败: " + e.getMessage()); // 控制台输出错误
        }
    }

    // 支持带异常堆栈的日志
    public void error(String message, Throwable throwable) { // 输出带异常栈的错误日志
        log("ERROR", message + " " + throwableToString(throwable)); // 日志加堆栈
    }

    private String throwableToString(Throwable t) { // 将异常转换为字符串
        StringBuilder sb = new StringBuilder(); // 用于拼接字符串
        sb.append("Exception: ").append(t.toString()).append("\n"); // 添加异常基本信息
        for (StackTraceElement ste : t.getStackTrace()) { // 遍历异常栈
            sb.append("\tat ").append(ste.toString()).append("\n"); // 添加每一行栈
        }
        return sb.toString(); // 返回完整字符串
    }
}