package com.nextstep.nextsiheung.global.security;

import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.json.JsonMapper;

// 필터 단계에서 막힌 요청도 명세의 실패 응답 형식({success, code, message, data})으로 돌려준다
// - 로그인 필요: 401 UNAUTHORIZED, 토큰이 만료됐으면 401 TOKEN_EXPIRED
// - 권한 없음(일반 사용자의 관리자 API): 403 FORBIDDEN
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    public SecurityErrorHandler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException {
        Object tokenError = request.getAttribute(JwtAuthenticationFilter.TOKEN_ERROR);
        write(response, tokenError instanceof ErrorCode code ? code : ErrorCode.UNAUTHORIZED);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
            throws IOException {
        write(response, ErrorCode.FORBIDDEN);
    }

    private void write(HttpServletResponse response, ErrorCode code) throws IOException {
        response.setStatus(code.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(jsonMapper.writeValueAsString(ApiResponse.fail(code, null)));
    }
}
