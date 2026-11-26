package com.nextstep.nextsiheung.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @Schema(example = "a@next-siheung.kr") @NotBlank @Email String email,
        @Schema(description = "8~20자, 영문·숫자·특수문자 포함", example = "next1234!")
        @NotBlank @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,20}$",
                message = "비밀번호는 8~20자, 영문·숫자·특수문자를 포함해야 합니다.") String password,
        @Schema(example = "A") @NotBlank @Size(max = 20) String nickname,
        @Schema(description = "선택. 19~24세 대상 안내용이며 범위 밖이어도 가입 허용", example = "2003") Integer birthYear,
        @Schema(example = "true") @AssertTrue boolean agreeTerms,
        @Schema(example = "true") @AssertTrue boolean agreePrivacy
) {
}
