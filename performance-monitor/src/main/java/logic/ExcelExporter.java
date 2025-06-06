package logic;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.sql.*;

public class ExcelExporter {
    public static void exportAbnormalData(String filePath) {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:performance.db");
                Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("性能异常数据");
            // 表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            // 表头
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "时间戳", "CPU使用率(%)", "内存使用率(%)", "磁盘使用率(%)", "温度(°C)" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            // 查询并写入数据
            String sql = "SELECT * FROM performance_data";
            try (Statement stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(sql)) {
                int rowNum = 1;
                while (rs.next()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(rs.getInt("id"));
                    row.createCell(1).setCellValue(rs.getString("timestamp"));
                    row.createCell(2).setCellValue(rs.getDouble("cpu_usage"));
                    row.createCell(3).setCellValue(rs.getDouble("memory_usage"));
                    row.createCell(4).setCellValue(rs.getDouble("disk_usage"));
                    row.createCell(5).setCellValue(rs.getDouble("temperature"));
                }
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage(), e);
        }
    }
}