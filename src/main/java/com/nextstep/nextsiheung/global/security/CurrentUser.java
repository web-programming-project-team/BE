package com.nextstep.nextsiheung.global.security;

import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

// 컨트롤러·서비스에서 로그인 사용자 ID를 꺼낸다
// - 로그인 필요 API(명세 권한 U): CurrentUser.id()
// - 로그인 선택 API(명세 권한 U?): CurrentUser.idOrNull() → 비로그인이면 null (myStatus 등을 null로)
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long id() {
        Long id = idOrNull();
        if (id == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return id;
    }

    public static Long idOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUser user) {
            return user.userId();
        }
        return null;
    }
}
