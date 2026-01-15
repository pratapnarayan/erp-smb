package com.erp.smb.product.service;

import com.erp.smb.common.dto.ImportError;
import com.erp.smb.common.dto.ImportResponse;
import com.erp.smb.product.domain.Item;
import com.erp.smb.product.repo.ItemRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OpeningStockImportService {

  private static final int MAX_ROWS = 5000;
  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

  private final ItemRepository itemRepository;

  public OpeningStockImportService(ItemRepository itemRepository) {
    this.itemRepository = itemRepository;
  }

  public ImportResponse importOpeningStock(MultipartFile file) throws IOException {
    // Validate file size
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new IllegalArgumentException("File size exceeds 5MB limit");
    }

    String filename = file.getOriginalFilename();
    if (filename == null) {
      throw new IllegalArgumentException("Invalid file");
    }

    List<String[]> rows;
    if (filename.endsWith(".csv")) {
      rows = parseCsv(file);
    } else if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
      rows = parseExcel(file);
    } else {
      throw new IllegalArgumentException("Unsupported file format. Use CSV or Excel");
    }

    return processRows(rows);
  }

  private List<String[]> parseCsv(MultipartFile file) throws IOException {
    try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
      return reader.readAll();
    } catch (CsvException e) {
      throw new IOException("Error parsing CSV file", e);
    }
  }

  private List<String[]> parseExcel(MultipartFile file) throws IOException {
    List<String[]> rows = new ArrayList<>();
    try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);
      for (Row row : sheet) {
        String[] rowData = new String[3]; // 3 columns
        for (int i = 0; i < 3; i++) {
          Cell cell = row.getCell(i);
          rowData[i] = getCellValueAsString(cell);
        }
        rows.add(rowData);
      }
    }
    return rows;
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

  private ImportResponse processRows(List<String[]> rows) {
    ImportResponse response = new ImportResponse();
    
    if (rows.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }

    // Skip header row
    int totalRows = rows.size() - 1;
    if (totalRows > MAX_ROWS) {
      throw new IllegalArgumentException("File exceeds maximum " + MAX_ROWS + " rows");
    }

    response.setTotalRows(totalRows);
    int successCount = 0;
    int failedCount = 0;

    for (int i = 1; i < rows.size(); i++) {
      String[] row = rows.get(i);
      try {
        boolean updated = validateAndUpdateStock(row, i);
        if (updated) {
          successCount++;
        }
      } catch (ValidationException e) {
        response.addError(new ImportError(i, e.getField(), e.getMessage()));
        failedCount++;
      }
    }

    response.setSuccessCount(successCount);
    response.setFailedCount(failedCount);
    return response;
  }

  private boolean validateAndUpdateStock(String[] row, int rowNumber) throws ValidationException {
    // Skip empty rows
    if (isEmptyRow(row)) return false;

    // product_name (required, must exist)
    String productName = getColumnValue(row, 0);
    if (productName == null || productName.isEmpty()) {
      throw new ValidationException("product_name", "Product name is required");
    }

    // Find product by name or SKU
    Optional<Item> itemOpt = itemRepository.findByName(productName);
    if (itemOpt.isEmpty()) {
      itemOpt = itemRepository.findBySku(productName);
    }
    
    if (itemOpt.isEmpty()) {
      throw new ValidationException("product_name", "Product not found: " + productName);
    }

    Item item = itemOpt.get();

    // quantity (required, numeric, > 0)
    String quantityStr = getColumnValue(row, 1);
    if (quantityStr == null || quantityStr.isEmpty()) {
      throw new ValidationException("quantity", "Quantity is required");
    }

    int quantity;
    try {
      // Handle decimal values from Excel by parsing as double first
      double quantityDouble = Double.parseDouble(quantityStr);
      quantity = (int) quantityDouble;
      if (quantity <= 0) {
        throw new ValidationException("quantity", "Quantity must be greater than 0");
      }
    } catch (NumberFormatException e) {
      throw new ValidationException("quantity", "Quantity must be a valid number");
    }

    // warehouse (optional, not used currently)
    String warehouse = getColumnValue(row, 2);
    // Warehouse field is optional and not processed in current implementation

    // Update stock
    item.setStock(item.getStock() + quantity);
    itemRepository.save(item);

    return true;
  }

  private String getColumnValue(String[] row, int index) {
    if (index >= row.length) return null;
    String value = row[index];
    return value == null ? null : value.trim();
  }

  private boolean isEmptyRow(String[] row) {
    for (String cell : row) {
      if (cell != null && !cell.trim().isEmpty()) {
        return false;
      }
    }
    return true;
  }

  public String generateTemplate() {
    return """
product_name,quantity,warehouse
Laptop Computer,50,Main Warehouse
Office Chair,100,Main Warehouse
A4 Paper Ream,500,Stationery Store
""";
  }

  private static class ValidationException extends Exception {
    private final String field;

    public ValidationException(String field, String message) {
      super(message);
      this.field = field;
    }

    public String getField() {
      return field;
    }
  }
}
