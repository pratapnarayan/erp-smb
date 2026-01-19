package com.erp.smb.common.imports;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel file parser implementation
 */
@Component
public class ExcelFileParser implements FileParser {
  
  private final int maxColumns;
  
  public ExcelFileParser() {
    this.maxColumns = 20; // default max columns
  }
  
  public ExcelFileParser(int maxColumns) {
    this.maxColumns = maxColumns;
  }
  
  @Override
  public List<String[]> parse(MultipartFile file) throws IOException {
    List<String[]> rows = new ArrayList<>();
    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);
      for (Row row : sheet) {
        String[] rowData = new String[maxColumns];
        for (int i = 0; i < maxColumns; i++) {
          Cell cell = row.getCell(i);
          rowData[i] = getCellValueAsString(cell);
        }
        rows.add(rowData);
      }
    }
    return rows;
  }
  
  @Override
  public boolean supports(String filename) {
    if (filename == null) return false;
    String lower = filename.toLowerCase();
    return lower.endsWith(".xlsx") || lower.endsWith(".xls");
  }
  
  private String getCellValueAsString(Cell cell) {
    if (cell == null) return "";
    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue().trim();
      case NUMERIC -> String.valueOf(cell.getNumericCellValue());
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      case FORMULA -> cell.getCellFormula();
      default -> "";
    };
  }
}
