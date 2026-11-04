package com.nextstep.nextsiheung.global.security;

import com.nextstep.nextsiheung.domain.user.service.UserVisitService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// 요청 헤더의 Bearer 토큰을 검증해 SecurityContext에 로그인 사용자를 넣는다.
// 토큰이 없거나 틀려도 여기서 막지 않는다. 막을지는 SecurityConfig의 경로 규칙이 정하고,
// 막힐 때 어떤 코드(TOKEN_EXPIRED / UNAUTHORIZED)로 응답할지는 요청 속성으로 SecurityErrorHandler에 넘긴다.
// (로그인 선택 API와 /auth/refresh는 만료된 토큰이 붙어 와도 그대로 통과해야 한다)
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String TOKEN_ERROR = JwtAuthenticationFilter.class.getName() + ".TOKEN_ERROR";
    private static final String BEARER = "Bearer ";

    private final JwtProvider jwtProvider;
    private final UserVisitService userVisitService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER)) {
            try {
                AuthUser user = jwtProvider.parse(header.substring(BEARER.length()).trim());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user, null, List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                recordVisit(user.userId());
            } catch (JwtProvider.InvalidTokenException e) {
                request.setAttribute(TOKEN_ERROR, e.getErrorCode());
            }
        }
        chain.doFilter(request, response);
    }

    // 접속 기록 실패가 요청 자체를 막으면 안 된다
    private void recordVisit(Long userId) {
        try {
            userVisitService.recordToday(userId);
        } catch (RuntimeException e) {
            log.warn("접속 기록 실패 userId={}", userId, e);
        }
    }
}
