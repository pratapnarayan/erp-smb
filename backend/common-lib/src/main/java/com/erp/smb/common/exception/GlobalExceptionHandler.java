package com.erp.smb.common.exception;

import com.erp.smb.common.dto.BaseResponse;
import com.erp.smb.common.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<ErrorDTO>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req){
        String msg = ex.getBindingResult().getFieldErrors().stream().findFirst().map(fe -> fe.getField()+": "+fe.getDefaultMessage()).orElse("Validation error");
        return ResponseEntity.badRequest().body(BaseResponse.error(msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<ErrorDTO>> handleGeneric(Exception ex, HttpServletRequest req){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BaseResponse.error("internal_error"));
    }
}
