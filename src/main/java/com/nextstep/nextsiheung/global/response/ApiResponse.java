package com.nextstep.nextsiheung.global.response;

import com.nextstep.nextsiheung.global.exception.ErrorCode;

public record ApiResponse<T>(boolean success, String code, String message, T data) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", "요청이 성공했습니다.", data);
    }

    public static ApiResponse<Void> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, T data) {
        return new ApiResponse<>(false, errorCode.name(), errorCode.getMessage(), data);
    }
}
