package ui; // 指定包名为ui，方便项目结构组织

import javafx.animation.AnimationTimer; // 导入JavaFX的动画计时器类，用于定时刷新UI
import javafx.application.Platform; // 导入JavaFX平台类，用于线程安全地更新UI
import javafx.fxml.FXML; // 导入FXML注解，标记FXML绑定的UI组件或方法
import javafx.scene.control.CheckMenuItem; // 导入JavaFX的勾选菜单项
import javafx.scene.control.Label; // 导入JavaFX的标签控件
import javafx.scene.image.Image; // 导入JavaFX的图片类
import javafx.scene.image.ImageView; // 导入JavaFX的图片视图控件
import javafx.scene.chart.LineChart; // 导入JavaFX的折线图控件
import javafx.scene.chart.NumberAxis; // 导入JavaFX的数字坐标轴
import javafx.scene.chart.XYChart; // 导入JavaFX的XY图表基础类
import javafx.stage.Stage; // 导入JavaFX的舞台类
import javafx.stage.WindowEvent; // 导入窗口事件类
import logic.HardwareMonitor; // 导入硬件监控逻辑类
import logic.HardwareMonitor.HardwareInfo; // 导入硬件信息内部类
import logic.PerformanceData; // 导入性能数据类

public class MainController { // 主控制器类，负责主界面交互逻辑

    // === FXML注入的UI组件（主界面元素） ===
    @FXML
    private Label cpuModelLabel; // CPU型号标签
    @FXML
    private Label diskModelLabel; // 磁盘型号标签
    @FXML
    private Label cpuUsageLabel; // CPU使用率标签
    @FXML
    private Label memoryUsageLabel; // 内存使用率标签
    @FXML
    private Label diskUsageLabel; // 磁盘使用率标签
    @FXML
    private Label temperatureLabel; // 温度标签
    @FXML
    private ImageView brandLogoView; // 品牌Logo视图

    // 新增UI组件
    @FXML
    private LineChart<Number, Number> usageChart; // 折线图显示CPU/内存/磁盘使用率随时间变化
    @FXML
    private CheckMenuItem cpuMenuItem; // 控制显示CPU曲线的菜单项
    @FXML
    private CheckMenuItem memoryMenuItem; // 控制显示内存曲线的菜单项
    @FXML
    private CheckMenuItem diskMenuItem; // 控制显示磁盘曲线的菜单项

    // 成员变量
    private HardwareMonitor monitor; // 硬件监视器对象，负责采集硬件信息
    private BrandLogoManager logoManager; // 品牌Logo管理器
    private AnimationTimer uiUpdateTimer; // 动画定时器，用于定时刷新UI数据

    // 折线图数据系列
    private XYChart.Series<Number, Number> cpuSeries; // CPU使用率数据曲线
    private XYChart.Series<Number, Number> memorySeries; // 内存使用率数据曲线
    private XYChart.Series<Number, Number> diskSeries; // 磁盘使用率数据曲线
    private int timeCounter = 0; // 用作X轴时间递增计数器
    private static final int MAX_DATA_POINTS = 60; // 折线图最多显示的数据点数（比如60秒）

    // 各项指标的异常阈值（与PerformanceData类中定义保持一致）
    private static final double CPU_THRESHOLD = 90.0; // CPU使用率阈值
    private static final double MEMORY_THRESHOLD = 85.0; // 内存使用率阈值
    private static final double DISK_THRESHOLD = 95.0; // 磁盘使用率阈值

    @FXML
    public void initialize() { // FXML自动调用的初始化方法，界面加载后执行
        try {
            // 初始化折线图
            initUsageChart();

            // 初始化监控逻辑
            monitor = new HardwareMonitor(); // 创建硬件监控对象
            logoManager = new BrandLogoManager(); // 创建Logo管理对象
            displayHardwareInfo(); // 显示硬件信息
            monitor.startMonitoring(2); // 启动硬件监控，2秒采样一次
            setupUIUpdateTimer(); // 启动UI定时刷新任务

            // 绑定窗口关闭事件，确保资源释放
            Stage stage = (Stage) cpuUsageLabel.getScene().getWindow(); // 获取窗口对象
            stage.setOnCloseRequest(this::handleWindowClose); // 绑定关闭事件
        } catch (Exception e) {
            e.printStackTrace();
            showError("初始化失败: " + e.getMessage()); // 异常提示
        }
    }

    // === 显示硬件信息（如CPU型号、磁盘型号及品牌Logo）===
    private void displayHardwareInfo() {
        try {
            HardwareInfo info = monitor.getHardwareInfo(); // 获取硬件信息
            cpuModelLabel.setText(info.cpuModel); // 显示CPU型号
            diskModelLabel.setText(info.diskModel); // 显示磁盘型号
            String brand = logoManager.detectBrandFromModel(info.cpuModel); // 根据CPU型号识别品牌
            Image logo = logoManager.getBrandLogo(brand); // 获取对应品牌Logo
            if (logo != null) {
                brandLogoView.setImage(logo); // 显示Logo
            }
        } catch (Exception e) {
            showError("无法获取硬件信息: " + e.getMessage());
        }
    }

    // 启动定时刷新UI动画，每秒更新一次UI
    private void setupUIUpdateTimer() {
        uiUpdateTimer = new AnimationTimer() { // 匿名内部类实现定时器
            private long lastUpdate = 0; // 上次更新时间
            @Override
            public void handle(long now) { // 每帧被JavaFX自动调用
                if (now - lastUpdate >= 1_000_000_000L) { // 达到1秒间隔
                    updateUIWithRealData(); // 刷新UI
                    lastUpdate = now; // 更新计时
                }
            }
        };
        uiUpdateTimer.start(); // 启动定时器
    }

    // 获取最新监控数据并更新UI
    private void updateUIWithRealData() {
        PerformanceData data = monitor.getLatestData(); // 获取最新性能数据
        if (data != null) {
            updateUI(data); // 更新UI
        }
    }

    // 主动更新UI显示，包括文本标签和折线图
    public void updateUI(PerformanceData data) {
        // 刷新标签显示
        cpuUsageLabel.setText(String.format("%.1f%%", data.getCpuUsage())); // CPU使用率
        memoryUsageLabel.setText(String.format("%.1f%%", data.getMemoryUsage())); // 内存使用率
        diskUsageLabel.setText(String.format("%.1f%%", data.getDiskUsage())); // 磁盘使用率
        temperatureLabel.setText(String.format("%.1f°C", data.getTemperature())); // 温度

        // 异常状态高亮
        if (data.isAbnormal()) {
            cpuUsageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;"); // 异常高亮
            memoryUsageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            diskUsageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            cpuUsageLabel.setStyle(""); // 恢复默认
            memoryUsageLabel.setStyle("");
            diskUsageLabel.setStyle("");
        }

        // 更新折线图曲线
        updateChart(data);
    }

    // === 导出异常数据到Excel ===
    @FXML
    private void handleExportExcel() {
        try {
            logic.ExcelExporter.exportAbnormalData("performance_abnormal.xlsx"); // 导出数据
            showInfo("数据已导出到 performance_abnormal.xlsx"); // 成功提示
        } catch (Exception e) {
            showError("导出失败: " + e.getMessage()); // 失败提示
        }
    }

    // 错误提示方法
    private void showError(String message) {
        System.err.println("[ERROR] " + message); // 控制台打印
        // 实际应用可弹窗提示用户
        cpuUsageLabel.setText("错误"); // 简单的视觉反馈
    }

    // 信息提示方法
    private void showInfo(String message) {
        System.out.println("[INFO] " + message); // 控制台打印
    }

    // 停止监控和UI刷新
    public void stopMonitoring() {
        if (monitor != null) {
            monitor.stopMonitoring(); // 停止硬件监控线程
        }
        if (uiUpdateTimer != null) {
            uiUpdateTimer.stop(); // 停止UI动画
        }
    }

    // === 新增方法：折线图初始化与交互 ===

    @SuppressWarnings("unchecked")
    private void initUsageChart() {
        // 清除图表现有数据系列
        usageChart.getData().clear();

        // 创建数据系列并命名
        cpuSeries = new XYChart.Series<>();
        cpuSeries.setName("CPU使用率");

        memorySeries = new XYChart.Series<>();
        memorySeries.setName("内存使用率");

        diskSeries = new XYChart.Series<>();
        diskSeries.setName("磁盘使用率");

        // 添加系列到折线图
        usageChart.getData().addAll(cpuSeries, memorySeries, diskSeries);

        // 设置初始可见性
        updateChartVisibility();
    }

    // 根据菜单项勾选状态显示/隐藏曲线
    private void updateChartVisibility() {
        if (cpuSeries != null) {
            cpuSeries.getNode().setVisible(cpuMenuItem.isSelected());
        }
        if (memorySeries != null) {
            memorySeries.getNode().setVisible(memoryMenuItem.isSelected());
        }
        if (diskSeries != null) {
            diskSeries.getNode().setVisible(diskMenuItem.isSelected());
        }
    }

    // FXML绑定：当用户切换勾选硬件类型时调用
    @FXML
    private void handleHardwareSelection() {
        updateChartVisibility();
    }

    // FXML绑定：重置曲线图
    @FXML
    private void handleResetChart() {
        if (cpuSeries != null)
            cpuSeries.getData().clear(); // 清空CPU数据
        if (memorySeries != null)
            memorySeries.getData().clear(); // 清空内存数据
        if (diskSeries != null)
            diskSeries.getData().clear(); // 清空磁盘数据
        timeCounter = 0; // 时间计数器归零
    }

    // 窗口关闭事件处理，优雅退出应用
    private void handleWindowClose(WindowEvent event) {
        stopMonitoring(); // 停止所有后台监控
        Platform.exit(); // 停止JavaFX线程
        System.exit(0); // 完全退出
    }

    // 更新折线图数据
    private void updateChart(PerformanceData data) {
        if (cpuSeries == null || memorySeries == null || diskSeries == null) {
            return; // 如果系列未初始化则返回
        }

        // 新增数据点到各曲线
        cpuSeries.getData().add(new XYChart.Data<>(timeCounter, data.getCpuUsage()));
        memorySeries.getData().add(new XYChart.Data<>(timeCounter, data.getMemoryUsage()));
        diskSeries.getData().add(new XYChart.Data<>(timeCounter, data.getDiskUsage()));

        // 控制数据点数量（只保留MAX_DATA_POINTS个点）
        if (cpuSeries.getData().size() > MAX_DATA_POINTS) {
            cpuSeries.getData().remove(0);
        }
        if (memorySeries.getData().size() > MAX_DATA_POINTS) {
            memorySeries.getData().remove(0);
        }
        if (diskSeries.getData().size() > MAX_DATA_POINTS) {
            diskSeries.getData().remove(0);
        }

        // 动态调整X轴显示范围
        if (usageChart.getXAxis() instanceof NumberAxis) {
            NumberAxis xAxis = (NumberAxis) usageChart.getXAxis();
            xAxis.setLowerBound(Math.max(0, timeCounter - MAX_DATA_POINTS));
            xAxis.setUpperBound(timeCounter);
        }

        timeCounter++; // 时间递增

        // 根据阈值改变曲线颜色
        updateChartColors(data);
    }

    // 根据实时数据动态设置折线颜色（高于阈值时变红）
    private void updateChartColors(PerformanceData data) {
        // CPU曲线变色
        if (data.getCpuUsage() > CPU_THRESHOLD) {
            cpuSeries.getNode().setStyle("-fx-stroke: red;");
        } else {
            cpuSeries.getNode().setStyle("");
        }

        // 内存曲线变色
        if (data.getMemoryUsage() > MEMORY_THRESHOLD) {
            memorySeries.getNode().setStyle("-fx-stroke: red;");
        } else {
            memorySeries.getNode().setStyle("");
        }

        // 磁盘曲线变色
        if (data.getDiskUsage() > DISK_THRESHOLD) {
            diskSeries.getNode().setStyle("-fx-stroke: red;");
        } else {
            diskSeries.getNode().setStyle("");
        }
    }
}
