package logic; // 声明当前类属于logic包，便于分层和管理

import java.io.FileInputStream; // 导入文件输入流，用于读取配置文件
import java.io.FileOutputStream; // 导入文件输出流，用于写入配置文件
import java.io.IOException; // 导入IO异常
import java.util.Properties; // 导入Properties类，管理配置项

/**
 * 配置文件管理类，支持读取和保存项目配置
 */
public class ConfigManager { // 配置管理器类定义
    private final Properties props; // Properties对象用于存储配置项
    private final String configFilePath; // 配置文件路径

    public ConfigManager(String configFilePath) { // 构造函数，指定配置文件路径
        this.configFilePath = configFilePath; // 设定配置文件路径
        this.props = new Properties(); // 初始化属性对象
        load(); // 自动加载配置
    }

    public String getConfig(String key, String defaultValue) { // 获取配置项值
        return props.getProperty(key, defaultValue); // 返回配置项或默认值
    }

    public void setConfig(String key, String value) { // 设置配置项
        props.setProperty(key, value); // 设置属性
    }

    public void save() { // 保存当前配置到文件
        try (FileOutputStream fos = new FileOutputStream(configFilePath)) { // 打开文件输出流
            props.store(fos, "Application Config"); // 保存属性到文件
        } catch (IOException e) { // 捕获异常
            System.err.println("保存配置文件失败: " + e.getMessage()); // 控制台输出错误
        }
    }

    public void load() { // 加载配置文件内容
        try (FileInputStream fis = new FileInputStream(configFilePath)) { // 打开文件输入流
            props.load(fis); // 加载属性
        } catch (IOException e) { // 捕获异常
            // 文件不存在等情况忽略
        }
    }
}