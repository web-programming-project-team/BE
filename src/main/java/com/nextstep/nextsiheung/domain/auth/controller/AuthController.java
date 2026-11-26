package com.nextstep.nextsiheung.domain.auth.controller;

import com.nextstep.nextsiheung.domain.auth.dto.LoginRequest;
import com.nextstep.nextsiheung.domain.auth.dto.LoginResponse;
import com.nextstep.nextsiheung.domain.auth.dto.MeResponse;
import com.nextstep.nextsiheung.domain.auth.dto.SignupRequest;
import com.nextstep.nextsiheung.domain.auth.dto.SignupResponse;
import com.nextstep.nextsiheung.domain.auth.dto.TokenRefreshRequest;
import com.nextstep.nextsiheung.domain.auth.dto.TokenResponse;
import com.nextstep.nextsiheung.domain.auth.service.AuthService;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import com.nextstep.nextsiheung.global.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController implements AuthApiDocs {

    private final AuthService authService;

    @Override
    @PostMapping("/auth/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponse> signup(@RequestBody SignupRequest request) {
        return ApiResponse.ok(authService.signup(request));
    }

    @Override
    @PostMapping("/auth/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @Override
    @PostMapping("/auth/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestBody TokenRefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request.refreshToken()));
    }

    @Override
    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        authService.logout(CurrentUser.id());
    }

    @Override
    @GetMapping("/users/me")
    public ApiResponse<MeResponse> me() {
        return ApiResponse.ok(authService.me(CurrentUser.id()));
    }

    @Override
    @DeleteMapping("/users/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw() {
        authService.withdraw(CurrentUser.id());
    }
}
