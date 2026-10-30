package com.nextstep.nextsiheung.domain.auth.controller;

import static com.nextstep.nextsiheung.global.exception.ErrorCode.DUPLICATE_EMAIL;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.INVALID_INPUT;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.LOGIN_FAILED;
import static com.nextstep.nextsiheung.global.exception.ErrorCode.UNAUTHORIZED;

import com.nextstep.nextsiheung.domain.auth.dto.LoginRequest;
import com.nextstep.nextsiheung.domain.auth.dto.LoginResponse;
import com.nextstep.nextsiheung.domain.auth.dto.MeResponse;
import com.nextstep.nextsiheung.domain.auth.dto.SignupRequest;
import com.nextstep.nextsiheung.domain.auth.dto.SignupResponse;
import com.nextstep.nextsiheung.domain.auth.dto.TokenRefreshRequest;
import com.nextstep.nextsiheung.domain.auth.dto.TokenResponse;
import com.nextstep.nextsiheung.global.exception.ApiErrorCodes;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "인증·회원", description = "이메일 가입과 JWT 로그인. 민감정보(주민번호, 소득)는 받지 않는다. 담당: 한동혁 · 6주차")
public interface AuthApiDocs {

    @Operation(summary = "회원가입 (1순위)", description = """
            이메일, 비밀번호, 닉네임으로 가입한다. 가입 직후 onboardingCompleted는 false이고, 화면은 온보딩으로 이동한다.
            - 주민등록번호·소득 정보는 받지 않음(제안서 4-1 ①)""")
    @SecurityRequirements
    @ApiErrorCodes({INVALID_INPUT, DUPLICATE_EMAIL})
    ApiResponse<SignupResponse> signup(@Valid SignupRequest request);

    @Operation(summary = "로그인 (1순위)", description = "Access·Refresh 토큰을 발급한다. onboardingCompleted가 false면 화면은 온보딩으로 보낸다. 로그인하면 users.last_login_at을 갱신한다.")
    @SecurityRequirements
    @ApiErrorCodes({LOGIN_FAILED})
    ApiResponse<LoginResponse> login(@Valid LoginRequest request);

    @Operation(summary = "토큰 재발급 (1순위)", description = "Refresh 토큰으로 새 토큰 쌍을 받는다. Refresh 토큰도 함께 교체(rotation)한다.")
    @SecurityRequirements
    @ApiErrorCodes({UNAUTHORIZED})
    ApiResponse<TokenResponse> refresh(@Valid TokenRefreshRequest request);

    @Operation(summary = "로그아웃 (1순위)", description = "서버에 저장된 Refresh 토큰을 삭제한다.")
    void logout();

    @Operation(summary = "내 계정 정보 (1순위)", description = "헤더 프로필과 마이페이지에 쓰는 기본 정보.")
    ApiResponse<MeResponse> me();

    @Operation(summary = "회원 탈퇴 (2순위)", description = "계정과 로드맵, 일정, 알림을 삭제한다. 작성한 게시글은 '탈퇴한 사용자'로 익명 처리해 남긴다.")
    void withdraw();
}
