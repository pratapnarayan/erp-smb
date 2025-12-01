package com.erp.smb.common.dto;

public record BaseResponse<T>(boolean success, String message, T data) {
    public static <T> BaseResponse<T> ok(T data){ return new BaseResponse<>(true, null, data);} 
    public static <T> BaseResponse<T> error(String message){ return new BaseResponse<>(false, message, null);} 
}
