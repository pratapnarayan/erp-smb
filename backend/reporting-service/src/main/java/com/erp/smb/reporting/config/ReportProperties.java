package com.erp.smb.reporting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "reports")
public class ReportProperties {
    private String storageDir;
    private long maxExportRows;
    private int exportRetentionDays;

    public String getStorageDir() { return storageDir; }
    public void setStorageDir(String storageDir) { this.storageDir = storageDir; }
    public long getMaxExportRows() { return maxExportRows; }
    public void setMaxExportRows(long maxExportRows) { this.maxExportRows = maxExportRows; }
    public int getExportRetentionDays() { return exportRetentionDays; }
    public void setExportRetentionDays(int exportRetentionDays) { this.exportRetentionDays = exportRetentionDays; }
}
