package com.nextstep.nextsiheung.global.config;

import com.nextstep.nextsiheung.global.exception.ApiErrorCodes;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;

@Configuration
public class SwaggerConfig {

    private static final String JWT = "JWT";

    // 우측 상단 Authorize에 Access 토큰만 넣으면 모든 요청에 Bearer 헤더가 붙는다
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("NEXT 시흥 API")
                .version("v1.4")
                .description("""
                    기준 명세: https://web-programming-project-team.github.io/API-/

                    - 로그인이 필요한 API는 공통으로 401 `UNAUTHORIZED`·`TOKEN_EXPIRED`, 관리자 API는 403 `FORBIDDEN`을 돌려줄 수 있다.
                    - 서비스가 아직 없는 API는 501 `NOT_IMPLEMENTED`를 돌려준다."""))
            .components(new Components().addSecuritySchemes(JWT, new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList(JWT));
    }

    // @ApiErrorCodes에 적은 에러 코드를 상태코드별 응답으로 묶고, 코드마다 실패 응답 예시를 붙인다
    @Bean
    public OperationCustomizer errorCodeCustomizer() {
        return (operation, handlerMethod) -> {
            ApiErrorCodes annotation = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getMethod(), ApiErrorCodes.class);
            if (annotation == null) {
                return operation;
            }
            if (operation.getResponses() == null) {
                operation.setResponses(new ApiResponses());
            }
            for (ErrorCode code : annotation.value()) {
                String status = String.valueOf(code.getStatus().value());
                io.swagger.v3.oas.models.responses.ApiResponse response = operation.getResponses()
                        .computeIfAbsent(status, s -> new io.swagger.v3.oas.models.responses.ApiResponse()
                                .description("")
                                .content(new Content().addMediaType("application/json", new MediaType())));
                response.setDescription(response.getDescription().isEmpty()
                        ? code.name() : response.getDescription() + " · " + code.name());
                response.getContent().get("application/json").addExamples(code.name(),
                        new Example().summary(code.getMessage()).value(ApiResponse.fail(code, null)));
            }
            return operation;
        };
    }
}
