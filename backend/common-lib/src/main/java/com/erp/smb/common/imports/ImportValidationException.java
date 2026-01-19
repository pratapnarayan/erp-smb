package com.erp.smb.common.imports;

/**
 * Exception thrown when validation fails during import
 */
public class ImportValidationException extends Exception {
  
  private final String field;
  
  public ImportValidationException(String field, String message) {
    super(message);
    this.field = field;
  }
  
  public String getField() {
    return field;
  }
}
