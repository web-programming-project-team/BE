package com.nextstep.nextsiheung.global.security;

import com.nextstep.nextsiheung.global.common.enums.Role;

// Access 토큰에서 꺼낸 로그인 사용자. SecurityContext의 principal로 들어간다
public record AuthUser(Long userId, Role role) {
}
