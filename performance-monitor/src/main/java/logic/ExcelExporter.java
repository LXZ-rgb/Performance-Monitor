package logic; // 指定包名为logic，方便逻辑代码管理

import java.io.FileOutputStream; // 导入文件输出流，用于写入Excel文件
import java.sql.*; // 导入JDBC相关包
import org.apache.poi.ss.usermodel.*; // 导入POI的通用表格相关类
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // 导入POI用于操作xlsx格式的Excel文件

public class ExcelExporter { // Excel导出工具类，负责将异常数据导出为Excel表格

    // 静态方法，将数据库中的异常性能数据导出到Excel文件
    public static void exportAbnormalData(String filePath) throws Exception {
        // 连接数据库，使用DatabaseHandler静态方法获取数据库路径
        String dbPath = DatabaseHandler.getDatabasePath();
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) { // 自动关闭连接
            // 查询异常数据：CPU>90%、内存>85%、磁盘>95%
            String sql = """
                    SELECT timestamp, cpu_usage, memory_usage, disk_usage, temperature
                    FROM performance_data
                    WHERE cpu_usage > 90
                       OR memory_usage > 85
                       OR disk_usage > 95
                    """;
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery();
                 Workbook wb = new XSSFWorkbook()) { // 创建XSSF工作簿（xlsx格式）
                Sheet sheet = wb.createSheet("Abnormal Data"); // 创建表单

                // 创建表头行
                Row header = sheet.createRow(0); // 第0行
                header.createCell(0).setCellValue("时间戳"); // 设置表头
                header.createCell(1).setCellValue("CPU使用率");
                header.createCell(2).setCellValue("内存使用率");
                header.createCell(3).setCellValue("磁盘使用率");
                header.createCell(4).setCellValue("温度");

                int rowNum = 1; // 数据行号从1开始
                while (rs.next()) { // 遍历结果集
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rs.getString("timestamp")); // 时间戳
                    row.createCell(1).setCellValue(rs.getDouble("cpu_usage")); // CPU
                    row.createCell(2).setCellValue(rs.getDouble("memory_usage")); // 内存
                    row.createCell(3).setCellValue(rs.getDouble("disk_usage")); // 磁盘
                    row.createCell(4).setCellValue(rs.getDouble("temperature")); // 温度
                }

                // 自动调整列宽
                for (int i = 0; i <= 4; i++) {
                    sheet.autoSizeColumn(i);
                }

                // 写入Excel文件
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    wb.write(fos); // 将工作簿内容写入文件
                }
            }
        }
    }
}
