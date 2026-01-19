package com.erp.smb.common.dto;

public class ImportError {
  private int rowNumber;
  private String field;
  private String reason;

  public ImportError() {}

  public ImportError(int rowNumber, String field, String reason) {
    this.rowNumber = rowNumber;
    this.field = field;
    this.reason = reason;
  }

  public int getRowNumber() { return rowNumber; }
  public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }

  public String getField() { return field; }
  public void setField(String field) { this.field = field; }

  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
}
