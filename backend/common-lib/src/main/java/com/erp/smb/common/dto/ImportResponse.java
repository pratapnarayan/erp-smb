package com.erp.smb.common.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResponse {
  private int totalRows;
  private int successCount;
  private int failedCount;
  private List<ImportError> errors = new ArrayList<>();

  public ImportResponse() {}

  public ImportResponse(int totalRows, int successCount, int failedCount) {
    this.totalRows = totalRows;
    this.successCount = successCount;
    this.failedCount = failedCount;
  }

  public int getTotalRows() { return totalRows; }
  public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

  public int getSuccessCount() { return successCount; }
  public void setSuccessCount(int successCount) { this.successCount = successCount; }

  public int getFailedCount() { return failedCount; }
  public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

  public List<ImportError> getErrors() { return errors; }
  public void setErrors(List<ImportError> errors) { this.errors = errors; }

  public void addError(ImportError error) {
    this.errors.add(error);
  }
}
