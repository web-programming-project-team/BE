package com.nextstep.nextsiheung.domain.auth.controller;

import com.nextstep.nextsiheung.domain.auth.dto.LoginRequest;
import com.nextstep.nextsiheung.domain.auth.dto.LoginResponse;
import com.nextstep.nextsiheung.domain.auth.dto.MeResponse;
import com.nextstep.nextsiheung.domain.auth.dto.SignupRequest;
import com.nextstep.nextsiheung.domain.auth.dto.SignupResponse;
import com.nextstep.nextsiheung.domain.auth.dto.TokenRefreshRequest;
import com.nextstep.nextsiheung.domain.auth.dto.TokenResponse;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class AuthController implements AuthApiDocs {

    @Override
    @PostMapping("/auth/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SignupResponse> signup(@RequestBody SignupRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PostMapping("/auth/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PostMapping("/auth/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestBody TokenRefreshRequest request) {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @PostMapping("/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @GetMapping("/users/me")
    public ApiResponse<MeResponse> me() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }

    @Override
    @DeleteMapping("/users/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw() {
        throw new BusinessException(ErrorCode.NOT_IMPLEMENTED);
    }
}
