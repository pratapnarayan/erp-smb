package com.erp.smb.common.imports;

import com.erp.smb.common.dto.ImportError;
import com.erp.smb.common.dto.ImportResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Abstract base class for import services
 * Provides common functionality for file parsing, validation, and processing
 * 
 * @param <T> The entity type being imported
 */
public abstract class AbstractImportService<T> {
  
  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
  private static final int MAX_ROWS = 5000;
  
  private final List<FileParser> fileParsers;
  
  protected AbstractImportService(List<FileParser> fileParsers) {
    this.fileParsers = fileParsers;
  }
  
  /**
   * Import entities from a file
   * @param file The uploaded file
   * @return ImportResponse with results
   * @throws IOException if file processing fails
   */
  public ImportResponse importFromFile(MultipartFile file) throws IOException {
    // Validate file size
    if (file.getSize() > getMaxFileSize()) {
      throw new IllegalArgumentException("File size exceeds " + (getMaxFileSize() / 1024 / 1024) + "MB limit");
    }
    
    String filename = file.getOriginalFilename();
    if (filename == null) {
      throw new IllegalArgumentException("Invalid file");
    }
    
    // Find appropriate parser
    FileParser parser = fileParsers.stream()
        .filter(p -> p.supports(filename))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unsupported file format. Use CSV or Excel"));
    
    // Parse file
    List<String[]> rows = parser.parse(file);
    
    // Process rows
    return processRows(rows);
  }
  
  /**
   * Process parsed rows and create entities
   * @param rows Parsed rows from file
   * @return ImportResponse with results
   */
  protected ImportResponse processRows(List<String[]> rows) {
    ImportResponse response = new ImportResponse();
    
    if (rows.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }
    
    // Skip header row
    int totalRows = rows.size() - 1;
    if (totalRows > getMaxRows()) {
      throw new IllegalArgumentException("File exceeds maximum " + getMaxRows() + " rows");
    }
    
    response.setTotalRows(totalRows);
    int successCount = 0;
    int failedCount = 0;
    
    for (int i = 1; i < rows.size(); i++) {
      String[] row = rows.get(i);
      try {
        T entity = validateAndCreateEntity(row, i);
        if (entity != null) {
          saveEntity(entity);
          successCount++;
        }
      } catch (ImportValidationException e) {
        response.addError(new ImportError(i, e.getField(), e.getMessage()));
        failedCount++;
      }
    }
    
    response.setSuccessCount(successCount);
    response.setFailedCount(failedCount);
    return response;
  }
  
  /**
   * Validate a row and create an entity
   * Override this method to implement domain-specific validation
   * 
   * @param row The row data
   * @param rowNumber The row number (for error reporting)
   * @return The created entity, or null to skip this row
   * @throws ImportValidationException if validation fails
   */
  protected abstract T validateAndCreateEntity(String[] row, int rowNumber) throws ImportValidationException;
  
  /**
   * Save the entity to the database
   * Override this method to implement domain-specific persistence
   * 
   * @param entity The entity to save
   */
  protected abstract void saveEntity(T entity);
  
  /**
   * Generate a CSV template for this import type
   * Override this method to provide domain-specific template
   * 
   * @return CSV template as string
   */
  public abstract String generateTemplate();
  
  /**
   * Get maximum file size allowed
   * Override to customize
   * 
   * @return Maximum file size in bytes
   */
  protected long getMaxFileSize() {
    return MAX_FILE_SIZE;
  }
  
  /**
   * Get maximum number of rows allowed
   * Override to customize
   * 
   * @return Maximum number of rows
   */
  protected int getMaxRows() {
    return MAX_ROWS;
  }
  
  /**
   * Get value from column, handling array bounds
   * 
   * @param row The row data
   * @param index The column index
   * @return The trimmed value, or null if not present
   */
  protected String getColumnValue(String[] row, int index) {
    if (index >= row.length) return null;
    String value = row[index];
    return value == null ? null : value.trim();
  }
  
  /**
   * Check if a row is empty (all cells are blank)
   * 
   * @param row The row data
   * @return true if row is empty
   */
  protected boolean isEmptyRow(String[] row) {
    for (String cell : row) {
      if (cell != null && !cell.trim().isEmpty()) {
        return false;
      }
    }
    return true;
  }
}
