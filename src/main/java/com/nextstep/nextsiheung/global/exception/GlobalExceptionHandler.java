package com.nextstep.nextsiheung.global.exception;

import com.nextstep.nextsiheung.global.response.ApiResponse;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.getStatus()).body(ApiResponse.fail(code, null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, List<Map<String, String>>>>> handleValid(
            MethodArgumentNotValidException e) {
        List<Map<String, String>> errors = e.getBindingResult().getFieldErrors().stream()
                .map(f -> fieldError(f.getField(), f.getDefaultMessage()))
                .toList();
        return invalidInput(errors);
    }

    // 쿼리·경로 값의 @Min, @Max 등 위반 (예: size=100)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Map<String, List<Map<String, String>>>>> handleMethodValidation(
            HandlerMethodValidationException e) {
        List<Map<String, String>> errors = e.getParameterValidationResults().stream()
                .flatMap(r -> r.getResolvableErrors().stream()
                        .map(err -> fieldError(r.getMethodParameter().getParameterName(), err.getDefaultMessage())))
                .toList();
        return invalidInput(errors);
    }

    // 쿼리·경로 값의 타입이나 enum 값이 틀림 (예: field=NOPE)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Map<String, List<Map<String, String>>>>> handleTypeMismatch(
            MethodArgumentTypeMismatchException e) {
        return invalidInput(List.of(fieldError(e.getName(), "값의 형식이 올바르지 않습니다.")));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Map<String, List<Map<String, String>>>>> handleMissingParam(
            MissingServletRequestParameterException e) {
        return invalidInput(List.of(fieldError(e.getParameterName(), "필수 값입니다.")));
    }

    // 본문 JSON 형식이 틀렸거나 enum에 없는 값
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Map<String, List<Map<String, String>>>>> handleNotReadable(
            HttpMessageNotReadableException e) {
        return invalidInput(List.of(fieldError("body", "요청 본문을 읽을 수 없습니다. JSON 형식과 enum 값을 확인해 주세요.")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleEtc(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.internalServerError().body(ApiResponse.fail(ErrorCode.INTERNAL_ERROR, null));
    }

    private static Map<String, String> fieldError(String field, String reason) {
        return Map.of("field", field, "reason", reason == null ? "" : reason);
    }

    private static ResponseEntity<ApiResponse<Map<String, List<Map<String, String>>>>> invalidInput(
            List<Map<String, String>> errors) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail(ErrorCode.INVALID_INPUT, Map.of("errors", errors)));
    }
}
