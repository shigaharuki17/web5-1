package com.tfzhang.quiz.controller;

import com.tfzhang.quiz.model.Result;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Result<Void>> business(ResponseStatusException error) {
        return ResponseEntity.status(error.getStatusCode()).body(Result.error(error.getReason()));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result<Void>> duplicate() { return ResponseEntity.status(HttpStatus.CONFLICT).body(Result.error("用户名已存在")); }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> validation(BindException error) {
        String message = error.getBindingResult().getFieldErrors().isEmpty() ? "参数校验失败" : error.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(Result.error(message));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Result<Void>> invalidInput() { return ResponseEntity.badRequest().body(Result.error("请求参数缺失或格式不正确")); }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Result<Void>> database() { return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Result.error("数据库暂时不可用，请稍后重试")); }
}
