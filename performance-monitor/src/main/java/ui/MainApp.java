package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            logException(e);
        });
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main_window.fxml"));
        Parent root = loader.load();
        
        primaryStage.setTitle("电脑性能监视器");
        primaryStage.setScene(new Scene(root, 800, 700));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        System.exit(0);
    }
    
    private static void logException(Throwable e) {
        try (FileWriter fw = new FileWriter("performance_monitor_error.log", true)) {
            fw.write(LocalDateTime.now() + ": Unhandled exception\n");
            fw.write("Message: " + e.getMessage() + "\n");
            for (StackTraceElement ste : e.getStackTrace()) {
                fw.write("\t" + ste.toString() + "\n");
            }
            fw.write("\n");
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }
    }
}