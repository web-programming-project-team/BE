package com.nextstep.nextsiheung.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String JWT = "JWT";

    // 우측 상단 Authorize에 Access 토큰만 넣으면 모든 요청에 Bearer 헤더가 붙는다
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("NEXT 시흥 API")
                .version("v1.2")
                .description("기준 명세: https://web-programming-project-team.github.io/API-/"))
            .components(new Components().addSecuritySchemes(JWT, new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList(JWT));
    }
}
