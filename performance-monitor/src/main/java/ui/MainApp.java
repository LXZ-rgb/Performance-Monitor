package ui; // 指定该类所在的包名为ui，便于项目结构管理和类的引用

import javafx.application.Application; // 导入JavaFX的Application类，JavaFX应用程序必须继承该类
import javafx.fxml.FXMLLoader; // 导入FXML加载器，用于加载FXML文件
import javafx.scene.Parent; // 导入JavaFX的Parent类，作为所有节点的基类
import javafx.scene.Scene; // 导入JavaFX的Scene类，表示舞台的场景内容
import javafx.stage.Stage; // 导入JavaFX的Stage类，表示主舞台窗口

import java.io.FileWriter; // 导入FileWriter类，用于写入日志文件
import java.io.IOException; // 导入IOException类，处理输入输出异常
import java.time.LocalDateTime; // 导入LocalDateTime类，用于获取当前时间

public class MainApp extends Application { // 定义MainApp类，继承自Application，JavaFX应用的入口
    @Override
    public void start(Stage primaryStage) throws Exception { // 重写start方法，JavaFX程序启动后自动调用
        // 设置全局异常处理器，捕获未被捕获的异常，防止程序崩溃时无日志信息
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            logException(e); // 调用日志记录方法，保存异常信息到日志文件
        });
        
        // 创建FXML加载器，加载主界面的FXML布局文件
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main_window.fxml")); // FXML用于可视化界面布局
        Parent root = loader.load(); // 加载FXML并生成界面节点树
        
        primaryStage.setTitle("电脑性能监视器"); // 设置主窗口标题
        primaryStage.setScene(new Scene(root, 800, 700)); // 创建并设置场景，窗口尺寸为800x700像素
        primaryStage.show(); // 显示主窗口
    }

    public static void main(String[] args) { // Java程序入口main方法
        launch(args); // 启动JavaFX应用，会自动调用start方法
    }

    @Override
    public void stop() { // 重写Application的stop方法，当应用关闭时调用
        System.exit(0); // 强制退出程序，确保所有线程退出
    }
    
    private static void logException(Throwable e) { // 定义静态方法，用于记录异常信息到日志文件
        try (FileWriter fw = new FileWriter("performance_monitor_error.log", true)) { // 以追加模式打开日志文件
            fw.write(LocalDateTime.now() + ": Unhandled exception\n"); // 写入当前时间和异常标记
            fw.write("Message: " + e.getMessage() + "\n"); // 写入异常消息
            for (StackTraceElement ste : e.getStackTrace()) { // 遍历异常堆栈信息
                fw.write("\t" + ste.toString() + "\n"); // 写入每一条堆栈信息
            }
            fw.write("\n"); // 换行分隔
        } catch (IOException ioEx) { // 捕获文件写入过程中的IO异常
            ioEx.printStackTrace(); // 打印异常信息到控制台
        }
    }
}
