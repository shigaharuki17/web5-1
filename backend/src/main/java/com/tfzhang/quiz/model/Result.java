package com.tfzhang.quiz.model;

public record Result<T>(int code, String msg, T data) {
    public static <T> Result<T> success(T data) { return new Result<>(1, "success", data); }
    public static <T> Result<T> success(String message, T data) { return new Result<>(1, message, data); }
    public static Result<Void> error(String message) { return new Result<>(0, message, null); }
}
