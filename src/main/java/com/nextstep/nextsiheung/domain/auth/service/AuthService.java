package com.nextstep.nextsiheung.domain.auth.service;

import com.nextstep.nextsiheung.domain.auth.dto.LoginRequest;
import com.nextstep.nextsiheung.domain.auth.dto.LoginResponse;
import com.nextstep.nextsiheung.domain.auth.dto.MeResponse;
import com.nextstep.nextsiheung.domain.auth.dto.SignupRequest;
import com.nextstep.nextsiheung.domain.auth.dto.SignupResponse;
import com.nextstep.nextsiheung.domain.auth.dto.TokenResponse;
import com.nextstep.nextsiheung.domain.auth.entity.RefreshToken;
import com.nextstep.nextsiheung.domain.auth.repository.RefreshTokenRepository;
import com.nextstep.nextsiheung.domain.user.entity.User;
import com.nextstep.nextsiheung.domain.user.repository.UserRepository;
import com.nextstep.nextsiheung.domain.user.service.UserVisitService;
import com.nextstep.nextsiheung.global.exception.BusinessException;
import com.nextstep.nextsiheung.global.exception.ErrorCode;
import com.nextstep.nextsiheung.global.security.JwtProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 회원가입·로그인·토큰 재발급·로그아웃·내 정보·탈퇴
// Refresh 토큰은 무작위 32바이트 문자열이고, DB(refresh_token)에는 SHA-256 해시만 저장한다
@Service
public class AuthService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserVisitService userVisitService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final long refreshValiditySeconds;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       UserVisitService userVisitService, PasswordEncoder passwordEncoder, JwtProvider jwtProvider,
                       @Value("${app.jwt.refresh-token-validity-seconds}") long refreshValiditySeconds) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userVisitService = userVisitService;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.refreshValiditySeconds = refreshValiditySeconds;
    }

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname().trim())
                .birthYear(request.birthYear())
                .agreeTerms(request.agreeTerms())
                .agreePrivacy(request.agreePrivacy())
                .build();
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            // 동시에 같은 이메일로 가입한 경우 (UNIQUE 제약)
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        return new SignupResponse(user.getId(), user.getEmail(), user.getNickname(), user.isOnboardingCompleted());
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalize(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPassword()))
                // 이메일이 없는지 비밀번호가 틀렸는지 구분하지 않는다
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        LocalDateTime now = now();
        user.recordLogin(now);
        refreshTokenRepository.deleteExpiredByUserId(user.getId(), now);
        String refreshToken = issueRefreshToken(user, now);
        userVisitService.recordToday(user.getId());

        return new LoginResponse(
                jwtProvider.createAccessToken(user.getId(), user.getRole()),
                refreshToken,
                jwtProvider.accessValiditySeconds(),
                new LoginResponse.LoginUser(user.getId(), user.getNickname(), user.getRole(), user.isOnboardingCompleted()));
    }

    // Refresh 토큰도 함께 교체(rotation)한다. 쓴 토큰은 바로 지워 다시 쓸 수 없다
    @Transactional
    public TokenResponse refresh(String refreshToken) {
        LocalDateTime now = now();
        RefreshToken saved = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        refreshTokenRepository.delete(saved);
        if (saved.isExpired(now)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        User user = saved.getUser();
        return new TokenResponse(
                jwtProvider.createAccessToken(user.getId(), user.getRole()),
                issueRefreshToken(user, now),
                jwtProvider.accessValiditySeconds());
    }

    // 이 회원의 Refresh 토큰을 모두 지운다 (Access 토큰은 만료(30분)까지 유효)
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    @Transactional(readOnly = true)
    public MeResponse me(Long userId) {
        User user = findUser(userId);
        return new MeResponse(user.getId(), user.getEmail(), user.getNickname(), user.getRole(),
                user.isOnboardingCompleted(), user.getCreatedAt());
    }

    // 회원 소유 데이터는 DB의 ON DELETE CASCADE로, 게시글 작성자 등은 SET NULL로 처리된다 (ERD 외래키 규칙)
    @Transactional
    public void withdraw(Long userId) {
        User user = findUser(userId);
        refreshTokenRepository.deleteAllByUserId(userId);
        userRepository.delete(user);
        userVisitService.forget(userId);
    }

    private User findUser(Long userId) {
        // 토큰은 유효하지만 이미 탈퇴한 회원
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    private String issueRefreshToken(User user, LocalDateTime now) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        refreshTokenRepository.save(new RefreshToken(user, hash(token), now.plusSeconds(refreshValiditySeconds), now));
        return token;
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static LocalDateTime now() {
        return LocalDateTime.now(SEOUL);
    }
}
