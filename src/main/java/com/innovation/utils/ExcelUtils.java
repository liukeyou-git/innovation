// src/main/java/com/innovation/utils/ExcelUtils.java
package com.innovation.utils;

import com.innovation.entity.Achievement;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}