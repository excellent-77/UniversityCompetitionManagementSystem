package com.competition.vo;

import lombok.Data;

@Data
public class ResultVO<T> {
    private Integer code;
    private String message;
    private T data;

    public ResultVO() {}

    public ResultVO(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>(200, "success", data);
    }

    public static <T> ResultVO<T> success(String message, T data) {
        return new ResultVO<>(200, message, data);
    }

    public static <T> ResultVO<T> success() {
        return new ResultVO<>(200, "success", null);
    }

    public static <T> ResultVO<T> error(Integer code, String message) {
        return new ResultVO<>(code, message, null);
    }

    public static <T> ResultVO<T> error(String message) {
        return new ResultVO<>(500, message, null);
    }
}