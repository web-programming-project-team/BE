package com.nextstep.nextsiheung.global.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Swagger 문서에 이 API가 돌려줄 수 있는 에러 코드를 표시한다 (SwaggerConfig가 상태코드별 예시로 바꿔 줌)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiErrorCodes {
    ErrorCode[] value();
}
