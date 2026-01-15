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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductImportService {

  private static final int MAX_ROWS = 5000;
  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

  private final ItemRepository itemRepository;

  public ProductImportService(ItemRepository itemRepository) {
    this.itemRepository = itemRepository;
  }

  public ImportResponse importProducts(MultipartFile file) throws IOException {
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
        String[] rowData = new String[7]; // 7 columns
        for (int i = 0; i < 7; i++) {
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
        Item item = validateAndCreateProduct(row, i);
        if (item != null) {
          itemRepository.save(item);
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

  private Item validateAndCreateProduct(String[] row, int rowNumber) throws ValidationException {
    // Skip empty rows
    if (isEmptyRow(row)) return null;

    Item item = new Item();

    // product_name (required)
    String productName = getColumnValue(row, 0);
    if (productName == null || productName.isEmpty()) {
      throw new ValidationException("product_name", "Product name is required");
    }
    item.setName(productName);

    // sku (optional, but generate if empty)
    String sku = getColumnValue(row, 1);
    if (sku == null || sku.isEmpty()) {
      sku = "SKU-" + System.currentTimeMillis() + "-" + rowNumber;
    }
    item.setSku(sku);

    // category (optional)
    String category = getColumnValue(row, 2);
    if (category != null && !category.isEmpty()) {
      item.setCategory(category);
    }

    // unit (optional)
    String unit = getColumnValue(row, 3);
    if (unit != null && !unit.isEmpty()) {
      item.setUnit(unit);
    }

    // cost_price (optional, numeric)
    String costPrice = getColumnValue(row, 4);
    if (costPrice != null && !costPrice.isEmpty()) {
      try {
        BigDecimal cost = new BigDecimal(costPrice);
        if (cost.compareTo(BigDecimal.ZERO) < 0) {
          throw new ValidationException("cost_price", "Cost price must be non-negative");
        }
        item.setCostPrice(cost);
      } catch (NumberFormatException e) {
        throw new ValidationException("cost_price", "Cost price must be a valid number");
      }
    }

    // selling_price (optional, numeric)
    String sellingPrice = getColumnValue(row, 5);
    if (sellingPrice != null && !sellingPrice.isEmpty()) {
      try {
        BigDecimal selling = new BigDecimal(sellingPrice);
        if (selling.compareTo(BigDecimal.ZERO) < 0) {
          throw new ValidationException("selling_price", "Selling price must be non-negative");
        }
        item.setSellingPrice(selling);
      } catch (NumberFormatException e) {
        throw new ValidationException("selling_price", "Selling price must be a valid number");
      }
    }

    // gst_rate (optional, 0-28)
    String gstRate = getColumnValue(row, 6);
    if (gstRate != null && !gstRate.isEmpty()) {
      try {
        BigDecimal gst = new BigDecimal(gstRate);
        if (gst.compareTo(BigDecimal.ZERO) < 0 || gst.compareTo(new BigDecimal("28")) > 0) {
          throw new ValidationException("gst_rate", "GST rate must be between 0 and 28");
        }
        item.setGstRate(gst);
      } catch (NumberFormatException e) {
        throw new ValidationException("gst_rate", "GST rate must be a valid number");
      }
    }

    // Set default values
    item.setStock(0);
    item.setReorder(0);
    item.setStatus("Active");

    return item;
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
product_name,sku,category,unit,cost_price,selling_price,gst_rate
Laptop Computer,LAP-001,Electronics,Unit,45000.00,55000.00,18
Office Chair,CHR-002,Furniture,Unit,3500.00,4500.00,12
A4 Paper Ream,PAP-003,Stationery,Ream,250.00,300.00,5
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
