package logic; // 声明当前类属于logic包

import java.io.File; // 导入File类，处理文件和目录
import java.io.FileInputStream; // 导入文件输入流
import java.io.FileOutputStream; // 导入文件输出流
import java.io.IOException; // 导入IO异常

/**
 * 文件操作工具类，提供常用文件相关方法
 */
public class FileUtils { // 文件工具类定义

    public static boolean exists(String path) { // 检查指定路径是否存在
        if (path == null)
            return false; // 路径为null直接返回false
        return new File(path).exists(); // 用File.exists判断
    }

    public static boolean delete(String path) { // 删除指定路径文件或目录
        if (path == null)
            return false; // 路径为null直接false
        File file = new File(path); // 创建File对象
        if (!file.exists())
            return false; // 不存在直接返回
        if (file.isDirectory()) { // 是目录则递归删除
            for (File child : file.listFiles()) { // 遍历子文件
                delete(child.getAbsolutePath()); // 递归删除
            }
        }
        return file.delete(); // 删除自身
    }

    public static boolean copy(String src, String dest) { // 复制文件
        if (src == null || dest == null)
            return false; // 源或目标为null返回false
        File srcFile = new File(src); // 源文件对象
        if (!srcFile.exists() || srcFile.isDirectory())
            return false; // 源不存在或为目录不支持
        try (FileInputStream fis = new FileInputStream(src); // 输入流
                FileOutputStream fos = new FileOutputStream(dest)) { // 输出流
            byte[] buf = new byte[1024]; // 缓冲区
            int len; // 实际读取长度
            while ((len = fis.read(buf)) != -1) { // 循环读取
                fos.write(buf, 0, len); // 写入目标
            }
            return true; // 成功返回true
        } catch (IOException e) { // 捕获IO异常
            return false; // 失败返回false
        }
    }

    public static long size(String path) { // 获取文件大小
        if (path == null)
            return 0; // 路径为null返回0
        File file = new File(path); // 创建文件对象
        if (!file.exists() || file.isDirectory())
            return 0; // 不存在或为目录返回0
        return file.length(); // 返回文件长度
    }

    public static boolean createFile(String path) { // 创建新文件
        if (path == null)
            return false; // 路径为null
        File file = new File(path); // 文件对象
        if (file.exists())
            return false; // 已存在直接返回
        try {
            return file.createNewFile(); // 创建新文件
        } catch (IOException e) {
            return false; // 失败返回false
        }
    }

    public static boolean createDir(String path) { // 创建目录
        if (path == null)
            return false; // 路径为null
        File dir = new File(path); // 目录对象
        if (dir.exists())
            return false; // 已存在直接返回
        return dir.mkdirs(); // 创建多级目录
    }
}