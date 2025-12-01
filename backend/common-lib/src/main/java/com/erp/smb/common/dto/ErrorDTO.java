package com.erp.smb.common.dto;

import java.time.Instant;

public record ErrorDTO(String code, String message, Instant timestamp, String path) {
    public static ErrorDTO of(String code, String message, String path){ return new ErrorDTO(code, message, Instant.now(), path);} 
}
