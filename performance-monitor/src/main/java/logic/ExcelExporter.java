package logic; // 声明包名

import org.apache.poi.ss.usermodel.*; // 导入POI的表格处理相关类
import org.apache.poi.xssf.usermodel.XSSFWorkbook; // 导入POI的XLSX工作簿类

import java.io.FileOutputStream; // 导入文件输出流
import java.sql.*; // 导入JDBC相关类

public class ExcelExporter { // 定义Excel导出工具类
    public static void exportAbnormalData(String filePath) { // 静态方法，用于导出异常数据到Excel
        try (
            // 获取数据库连接，使用sqlite和自定义数据库路径
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DatabaseHandler.getDatabasePath());
            // 创建XLSX格式的工作簿
            Workbook workbook = new XSSFWorkbook()
        ) {

            // 创建一个新的工作表，命名为"性能异常数据"
            Sheet sheet = workbook.createSheet("性能异常数据");
            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont(); // 创建字体对象
            headerFont.setBold(true); // 设置字体加粗
            headerStyle.setFont(headerFont); // 应用字体到样式
            // 创建表头行
            Row headerRow = sheet.createRow(0);
            // 定义表头内容
            String[] headers = { "ID", "时间戳", "CPU使用率(%)", "内存使用率(%)", "磁盘使用率(%)", "温度(°C)" };
            // 填充表头单元格
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i); // 创建单元格
                cell.setCellValue(headers[i]); // 设置表头内容
                cell.setCellStyle(headerStyle); // 设置表头样式
            }
            // 构建查询SQL
            String sql = "SELECT * FROM performance_data";
            try (
                Statement stmt = conn.createStatement(); // 创建SQL语句对象
                ResultSet rs = stmt.executeQuery(sql) // 执行查询获得结果集
            ) {
                int rowNum = 1; // 数据行从第1行（第二行）开始
                // 遍历查询结果，写入每一行数据
                while (rs.next()) {
                    Row row = sheet.createRow(rowNum++); // 创建新行
                    row.createCell(0).setCellValue(rs.getInt("id")); // 写入ID
                    row.createCell(1).setCellValue(rs.getString("timestamp")); // 写入时间戳
                    row.createCell(2).setCellValue(rs.getDouble("cpu_usage")); // 写入CPU使用率
                    row.createCell(3).setCellValue(rs.getDouble("memory_usage")); // 写入内存使用率
                    row.createCell(4).setCellValue(rs.getDouble("disk_usage")); // 写入磁盘使用率
                    row.createCell(5).setCellValue(rs.getDouble("temperature")); // 写入温度
                }
            }
            // 设置每一列自适应宽度
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            // 将数据写入指定文件路径的Excel文件
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        } catch (Exception e) {
            // 捕获异常并抛出运行时异常，包含错误信息
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }
}
