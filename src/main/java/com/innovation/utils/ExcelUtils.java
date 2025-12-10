// src/main/java/com/innovation/utils/ExcelUtils.java
package com.innovation.utils;

import com.innovation.common.ImportResult;
import com.innovation.entity.Achievement;
import jakarta.servlet.ServletOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ExcelUtils {
    public static void exportAchievements(List<Map<String, Object>> achievements,
                                          String fileName, HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             OutputStream os = new BufferedOutputStream(response.getOutputStream())) {

            Sheet sheet = workbook.createSheet("成绩列表");

            // 表头：修改为“成员姓名”“成员学号”
            String[] headers = {"项目ID", "项目名称", "成员姓名", "成员学号", "分数", "等级", "教师评语", "评定时间"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // 填充数据
            int rowNum = 1;
            if (achievements != null && !achievements.isEmpty()) {
                for (Map<String, Object> item : achievements) {
                    Row row = sheet.createRow(rowNum++);

                    // 项目信息
                    Map<String, Object> project = (Map<String, Object>) item.get("project");
                    if (project == null) project = new HashMap<>();

                    // 成绩信息
                    Achievement achievement = (Achievement) item.get("achievement");
                    if (achievement == null) achievement = new Achievement();

                    // 处理所有成员信息（拼接姓名和学号）
                    List<Map<String, Object>> members = (List<Map<String, Object>>) item.get("members");
                    if (members == null) members = new ArrayList<>();

                    List<String> memberNames = new ArrayList<>();
                    List<String> memberIds = new ArrayList<>();
                    for (Map<String, Object> member : members) {
                        memberNames.add(toStringSafe(member.get("realName")));
                        memberIds.add(toStringSafe(member.get("studentId")));
                    }
                    String allMemberNames = String.join(", ", memberNames);
                    String allMemberIds = String.join(", ", memberIds);

                    // 填充单元格
                    row.createCell(0).setCellValue(toStringSafe(project.get("projectId")));
                    row.createCell(1).setCellValue(toStringSafe(project.get("projectName")));
                    row.createCell(2).setCellValue(allMemberNames);
                    row.createCell(3).setCellValue(allMemberIds);
                    row.createCell(4).setCellValue(toStringSafe(achievement.getScore()));
                    row.createCell(5).setCellValue(toStringSafe(achievement.getGrade()));
                    row.createCell(6).setCellValue(toStringSafe(achievement.getTeacherComment()));
                    row.createCell(7).setCellValue(toStringSafe(achievement.getEvaluationTime()));
                }
            } else {
                // 无数据时提示
                Row emptyRow = sheet.createRow(1);
                emptyRow.createCell(0).setCellValue("无成绩数据");
            }

            // 自适应列宽（避免内容截断）
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 手动加宽列（防止中文内容显示不全）
                int width = sheet.getColumnWidth(i) * 11 / 10;
                sheet.setColumnWidth(i, width);
            }

            // 响应设置
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name());
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

            // 写入并刷新
            workbook.write(os);
            os.flush();
        }
    }

    private static String toStringSafe(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    // 生成成绩导入模板
    public static void generateTemplate(List<String[]> header, ServletOutputStream outputStream) {
        // 创建XSSFWorkbook（.xlsx格式）
        try (Workbook workbook = new XSSFWorkbook()) {
            // 1. 创建工作表（命名为“成绩导入模板”）
            Sheet sheet = workbook.createSheet("成绩导入模板");

            // 2. 创建表头样式（加粗、居中）
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true); // 加粗
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER); // 水平居中
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 垂直居中
            // 设置边框
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 3. 普通单元格样式（居中）
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);

            // 4. 填充表头和示例数据（header格式：[表头行, 示例数据行]）
            for (int rowIdx = 0; rowIdx < header.size(); rowIdx++) {
                String[] rowData = header.get(rowIdx);
                Row row = sheet.createRow(rowIdx); // 创建行

                for (int colIdx = 0; colIdx < rowData.length; colIdx++) {
                    Cell cell = row.createCell(colIdx); // 创建单元格
                    cell.setCellValue(rowData[colIdx]); // 设置单元格值

                    // 表头行用加粗样式，其他行用普通样式
                    cell.setCellStyle(rowIdx == 0 ? headerStyle : cellStyle);

                    // 自动调整列宽（根据内容长度）
                    sheet.autoSizeColumn(colIdx);
                }
            }

            // 5. 写入输出流（响应给前端）
            workbook.write(outputStream);
            outputStream.flush();

        } catch (IOException e) {
            throw new RuntimeException("生成Excel模板失败: " + e.getMessage());
        }
    }

    // 在ExcelUtils.java中添加以下方法
    /**
     * 解析成绩导入Excel
     */
    public static ImportResult<Achievement> parseAchievementImport(MultipartFile file) {
        ImportResult<Achievement> result = new ImportResult<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // 获取第一个工作表
            int rowNum = 0;

            // 验证表头
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                result.getErrorMessages().add("Excel文件无数据");
                return result;
            }

            String[] expectedHeaders = {"项目名称", "成员姓名", "成员学号", "分数", "等级", "教师评语"};
            for (int i = 0; i < expectedHeaders.length; i++) {
                Cell cell = headerRow.getCell(i);
                String actualHeader = cell != null ? cell.getStringCellValue().trim() : "";
                if (!actualHeader.equals(expectedHeaders[i])) {
                    result.getErrorMessages().add("表头格式错误，第" + (i+1) + "列应为：" + expectedHeaders[i]);
                    return result;
                }
            }

            // 解析数据行（从第2行开始，行号从1开始计数）
            for (rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue;

                try {
                    // 项目名称（第1列）
                    String projectName = getCellStringValue(row.getCell(0)).trim();
                    // 成员学号（第3列，逗号分隔）
                    String studentIds = getCellStringValue(row.getCell(2)).trim();
                    // 分数（第4列）
                    String scoreStr = getCellStringValue(row.getCell(3)).trim();
                    // 等级（第5列）
                    String grade = getCellStringValue(row.getCell(4)).trim();
                    // 教师评语（第6列）
                    String comment = getCellStringValue(row.getCell(5)).trim();

                    // 基础验证
                    List<String> rowErrors = new ArrayList<>();
                    if (projectName.isEmpty()) rowErrors.add("项目名称不能为空");
                    if (studentIds.isEmpty()) rowErrors.add("成员学号不能为空");
                    if (scoreStr.isEmpty()) {
                        rowErrors.add("分数不能为空");
                    } else {
                        try {
                            int score = Integer.parseInt(scoreStr);
                            if (score < 0 || score > 100) {
                                rowErrors.add("分数必须在0-100之间");
                            }
                        } catch (NumberFormatException e) {
                            rowErrors.add("分数格式错误，必须为数字");
                        }
                    }
                    if (grade.isEmpty()) {
                        rowErrors.add("等级不能为空");
                    } else {
                        List<String> validGrades = Arrays.asList("优秀", "良好", "中等", "及格", "不及格");
                        if (!validGrades.contains(grade)) {
                            rowErrors.add("等级必须是：优秀/良好/中等/及格/不及格");
                        }
                    }

                    // 收集行错误
                    if (!rowErrors.isEmpty()) {
                        result.getErrorMessages().add("行" + (rowNum + 1) + "：" + String.join("；", rowErrors));
                        continue;
                    }

                    // 封装成绩对象（projectId后续由Service层验证填充）
                    Achievement achievement = new Achievement();
                    achievement.setScore(Integer.parseInt(scoreStr));
                    achievement.setGrade(grade);
                    achievement.setTeacherComment(comment);
                    // 存储临时信息用于后续验证（项目名称和学生学号）
                    Map<String, Object> tempData = new HashMap<>();
                    tempData.put("projectName", projectName);
                    tempData.put("studentIds", studentIds);
                    achievement.setTempData(tempData); // 需要给Achievement添加一个临时数据字段

                    result.getValidData().add(achievement);
                } catch (Exception e) {
                    result.getErrorMessages().add("行" + (rowNum + 1) + "：解析错误 - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            result.getErrorMessages().add("文件解析失败：" + e.getMessage());
        }
        return result;
    }

    // 辅助方法：获取单元格字符串值
    private static String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}