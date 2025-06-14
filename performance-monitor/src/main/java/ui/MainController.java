package ui;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import logic.HardwareMonitor;
import logic.HardwareMonitor.HardwareInfo;
import logic.PerformanceData;

public class MainController {
    // 原有UI组件
    @FXML
    private Label cpuModelLabel;
    @FXML
    private Label diskModelLabel;
    @FXML
    private Label cpuUsageLabel;
    @FXML
    private Label memoryUsageLabel;
    @FXML
    private Label diskUsageLabel;
    @FXML
    private Label temperatureLabel;
    @FXML
    private ImageView brandLogoView;

    // 新增UI组件
    @FXML
    private LineChart<Number, Number> usageChart;
    @FXML
    private CheckMenuItem cpuMenuItem;
    @FXML
    private CheckMenuItem memoryMenuItem;
    @FXML
    private CheckMenuItem diskMenuItem;

    // 原有成员变量
    private HardwareMonitor monitor;
    private BrandLogoManager logoManager;
    private AnimationTimer uiUpdateTimer;

    // 折线图相关变量
    private XYChart.Series<Number, Number> cpuSeries;
    private XYChart.Series<Number, Number> memorySeries;
    private XYChart.Series<Number, Number> diskSeries;
    private int timeCounter = 0;
    private static final int MAX_DATA_POINTS = 60;

    // 阈值设置（与PerformanceData保持一致）
    private static final double CPU_THRESHOLD = 90.0;
    private static final double MEMORY_THRESHOLD = 85.0;
    private static final double DISK_THRESHOLD = 95.0;

    @FXML
    public void initialize() {
        try {
            // 初始化折线图
            initUsageChart();

            // 原有初始化逻辑
            monitor = new HardwareMonitor();
            logoManager = new BrandLogoManager();
            displayHardwareInfo();
            monitor.startMonitoring(2); // 2秒采样
            setupUIUpdateTimer();

            // 监听窗口关闭事件
            Stage stage = (Stage) cpuUsageLabel.getScene().getWindow();
            stage.setOnCloseRequest(this::handleWindowClose);
        } catch (Exception e) {
            e.printStackTrace();
            showError("初始化失败: " + e.getMessage());
        }
    }

    // === 原有方法 ===
    private void displayHardwareInfo() {
        try {
            HardwareInfo info = monitor.getHardwareInfo();
            cpuModelLabel.setText(info.cpuModel);
            diskModelLabel.setText(info.diskModel);
            String brand = logoManager.detectBrandFromModel(info.cpuModel);
            Image logo = logoManager.getBrandLogo(brand);
            if (logo != null) {
                brandLogoView.setImage(logo);
            }
        } catch (Exception e) {
            showError("无法获取硬件信息: " + e.getMessage());
        }
    }

    private void setupUIUpdateTimer() {
        uiUpdateTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 1_000_000_000L) { // 每秒
                    updateUIWithRealData();
                    lastUpdate = now;
                }
            }
        };
        uiUpdateTimer.start();
    }

    private void updateUIWithRealData() {
        PerformanceData data = monitor.getLatestData();
        if (data != null) {
            updateUI(data);
        }
    }

    public void updateUI(PerformanceData data) {
        // 更新文本标签
        cpuUsageLabel.setText(String.format("%.1f%%", data.getCpuUsage()));
        memoryUsageLabel.setText(String.format("%.1f%%", data.getMemoryUsage()));
        diskUsageLabel.setText(String.format("%.1f%%", data.getDiskUsage()));
        temperatureLabel.setText(String.format("%.1f°C", data.getTemperature()));

        // 异常状态标记
        if (data.isAbnormal()) {
            cpuUsageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            memoryUsageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            diskUsageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            cpuUsageLabel.setStyle("");
            memoryUsageLabel.setStyle("");
            diskUsageLabel.setStyle("");
        }

        // 新增：更新折线图
        updateChart(data);
    }

    @FXML
    private void handleExportExcel() {
        try {
            logic.ExcelExporter.exportAbnormalData("performance_abnormal.xlsx");
            showInfo("数据已导出到 performance_abnormal.xlsx");
        } catch (Exception e) {
            showError("导出失败: " + e.getMessage());
        }
    }

    private void showError(String message) {
        System.err.println("[ERROR] " + message);
        // 在实际应用中可替换为弹窗
        cpuUsageLabel.setText("错误");
    }

    private void showInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    public void stopMonitoring() {
        if (monitor != null) {
            monitor.stopMonitoring();
        }
        if (uiUpdateTimer != null) {
            uiUpdateTimer.stop();
        }
    }

    // === 新增方法 ===
    @SuppressWarnings("unchecked")
    private void initUsageChart() {
        // 清除现有数据
        usageChart.getData().clear();

        // 创建数据系列
        cpuSeries = new XYChart.Series<>();
        cpuSeries.setName("CPU使用率");

        memorySeries = new XYChart.Series<>();
        memorySeries.setName("内存使用率");

        diskSeries = new XYChart.Series<>();
        diskSeries.setName("磁盘使用率");

        // 添加系列到图表
        usageChart.getData().addAll(cpuSeries, memorySeries, diskSeries);

        // 设置初始可见性
        updateChartVisibility();
    }

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

    @FXML
    private void handleHardwareSelection() {
        updateChartVisibility();
    }

    @FXML
    private void handleResetChart() {
        if (cpuSeries != null)
            cpuSeries.getData().clear();
        if (memorySeries != null)
            memorySeries.getData().clear();
        if (diskSeries != null)
            diskSeries.getData().clear();
        timeCounter = 0;
    }

    private void handleWindowClose(WindowEvent event) {
        stopMonitoring();
        Platform.exit();
        System.exit(0);
    }

    private void updateChart(PerformanceData data) {
        if (cpuSeries == null || memorySeries == null || diskSeries == null) {
            return; // 确保系列已初始化
        }

        // 添加新数据点
        cpuSeries.getData().add(new XYChart.Data<>(timeCounter, data.getCpuUsage()));
        memorySeries.getData().add(new XYChart.Data<>(timeCounter, data.getMemoryUsage()));
        diskSeries.getData().add(new XYChart.Data<>(timeCounter, data.getDiskUsage()));

        // 移除旧数据点，保持固定长度
        if (cpuSeries.getData().size() > MAX_DATA_POINTS) {
            cpuSeries.getData().remove(0);
        }
        if (memorySeries.getData().size() > MAX_DATA_POINTS) {
            memorySeries.getData().remove(0);
        }
        if (diskSeries.getData().size() > MAX_DATA_POINTS) {
            diskSeries.getData().remove(0);
        }

        // 更新X轴范围
        if (usageChart.getXAxis() instanceof NumberAxis) {
            NumberAxis xAxis = (NumberAxis) usageChart.getXAxis();
            xAxis.setLowerBound(Math.max(0, timeCounter - MAX_DATA_POINTS));
            xAxis.setUpperBound(timeCounter);
        }

        timeCounter++;

        // 根据阈值设置折线颜色
        updateChartColors(data);
    }

    private void updateChartColors(PerformanceData data) {
        // CPU使用率颜色
        if (data.getCpuUsage() > CPU_THRESHOLD) {
            cpuSeries.getNode().setStyle("-fx-stroke: red;");
        } else {
            cpuSeries.getNode().setStyle("");
        }

        // 内存使用率颜色
        if (data.getMemoryUsage() > MEMORY_THRESHOLD) {
            memorySeries.getNode().setStyle("-fx-stroke: red;");
        } else {
            memorySeries.getNode().setStyle("");
        }

        // 磁盘使用率颜色
        if (data.getDiskUsage() > DISK_THRESHOLD) {
            diskSeries.getNode().setStyle("-fx-stroke: red;");
        } else {
            diskSeries.getNode().setStyle("");
        }
    }
}