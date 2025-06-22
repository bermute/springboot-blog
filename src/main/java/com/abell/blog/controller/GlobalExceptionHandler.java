package com.abell.blog.controller;

import com.abell.blog.dto.CustomErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;



@RestControllerAdvice
public class GlobalExceptionHandler {

    //  404 Not Found: 리소스를 못 찾았을 때
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new CustomErrorResponse("NOT_FOUND", e.getMessage()));
    }

    //  403 Forbidden: 권한 없음 (인증은 되었으나 권한이 없을 때)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CustomErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new CustomErrorResponse("FORBIDDEN", e.getMessage()));
    }

    //  500: 그 외 모든 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomErrorResponse("INTERNAL_ERROR", "서버 오류가 발생했습니다."));
    }
}