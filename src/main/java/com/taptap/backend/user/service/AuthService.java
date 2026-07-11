package com.taptap.backend.user.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.taptap.backend.button.repository.ButtonRepository;
import com.taptap.backend.config.AuthException;
import com.taptap.backend.config.GoogleTokenVerifier;
import com.taptap.backend.config.JwtProvider;
import com.taptap.backend.record.repository.ButtonRecordRepository;
import com.taptap.backend.reminder.repository.ReminderRepository;
import com.taptap.backend.template.repository.UserTemplateSelectionRepository;
import com.taptap.backend.user.dto.*;
import com.taptap.backend.user.entity.EmailVerification;
import com.taptap.backend.user.entity.RefreshToken;
import com.taptap.backend.user.entity.User;
import com.taptap.backend.user.repository.EmailVerificationRepository;
import com.taptap.backend.user.repository.RefreshTokenRepository;
import com.taptap.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserTemplateSelectionRepository userTemplateSelectionRepository;
    private final ButtonRepository buttonRepository;
    private final ButtonRecordRepository buttonRecordRepository;
    private final ReminderRepository reminderRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final GoogleTokenVerifier googleTokenVerifier;

    public AuthService(
            UserRepository userRepository,
            EmailVerificationRepository emailVerificationRepository,
            RefreshTokenRepository refreshTokenRepository,
            UserTemplateSelectionRepository userTemplateSelectionRepository,
            ButtonRepository buttonRepository,
            ButtonRecordRepository buttonRecordRepository,
            ReminderRepository reminderRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider,
            GoogleTokenVerifier googleTokenVerifier
    ) {
        this.userRepository = userRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userTemplateSelectionRepository = userTemplateSelectionRepository;
        this.buttonRepository = buttonRepository;
        this.buttonRecordRepository = buttonRecordRepository;
        this.reminderRepository = reminderRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndStatus(request.email(), "ACTIVE")) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다.");
        }

        EmailVerification verification = emailVerificationRepository
                .findTopByEmailOrderByCreatedAtDesc(request.email())
                .orElseThrow(() -> new AuthException(HttpStatus.BAD_REQUEST, "인증코드가 일치하지 않거나 만료되었습니다."));

        boolean invalid = Boolean.TRUE.equals(verification.getIsUsed())
                || !verification.getCode().equals(request.verificationCode())
                || verification.getExpiresAt().isBefore(LocalDateTime.now());

        if (invalid) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "인증코드가 일치하지 않거나 만료되었습니다.");
        }

        verification.setIsUsed(true);
        emailVerificationRepository.save(verification);

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setUsername(request.username());
        user.setLoginType("EMAIL");
        user.setStatus("ACTIVE");
        userRepository.save(user);

        String accessToken = jwtProvider.generateAccessToken(user.getUserId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());
        saveRefreshToken(user.getUserId(), refreshToken);

        return new RegisterResponse(
                user.getUserId(),
                user.getEmail(),
                user.getUsername(),
                accessToken,
                refreshToken,
                true,
                user.getCreatedAt()
        );
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "존재하지 않는 계정입니다."));

        if ("DELETED".equals(user.getStatus())) {
            throw new AuthException(HttpStatus.NOT_FOUND, "존재하지 않는 계정입니다.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getUserId());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());
        saveRefreshToken(user.getUserId(), refreshToken);

        boolean isOnboardingRequired = resolveOnboardingRequired(user.getUserId());

        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getUserId(),
                isOnboardingRequired
        );
    }

    @Transactional
    public TokenRefreshResponse refreshAccessToken(TokenRefreshRequest request) {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다."));

        if (Boolean.TRUE.equals(tokenEntity.getIsRevoked())) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "무효화된 Refresh Token입니다.");
        }

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "만료된 Refresh Token입니다.");
        }

        String newAccessToken = jwtProvider.generateAccessToken(tokenEntity.getUserId());
        return new TokenRefreshResponse(newAccessToken);
    }

    @Transactional
    public GoogleLoginResult googleLogin(GoogleLoginRequest request) {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(request.idToken());
        String googleSub = payload.getSubject();
        String email = payload.getEmail();

        return userRepository.findByGoogleSub(googleSub)
                .map(user -> {
                    if ("DELETED".equals(user.getStatus())) {
                        throw new AuthException(HttpStatus.NOT_FOUND, "존재하지 않는 계정입니다.");
                    }

                    String accessToken = jwtProvider.generateAccessToken(user.getUserId());
                    String refreshToken = jwtProvider.generateRefreshToken(user.getUserId());
                    saveRefreshToken(user.getUserId(), refreshToken);

                    boolean isOnboardingRequired = resolveOnboardingRequired(user.getUserId());

                    GoogleLoginResponse response = new GoogleLoginResponse(
                            accessToken, refreshToken, user.getUserId(), false, isOnboardingRequired
                    );
                    return new GoogleLoginResult(response, false);
                })
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setGoogleSub(googleSub);
                    newUser.setUsername(generateDefaultUsername(email));
                    newUser.setLoginType("GOOGLE");
                    newUser.setStatus("ACTIVE");
                    userRepository.save(newUser);

                    String accessToken = jwtProvider.generateAccessToken(newUser.getUserId());
                    String refreshToken = jwtProvider.generateRefreshToken(newUser.getUserId());
                    saveRefreshToken(newUser.getUserId(), refreshToken);

                    GoogleLoginResponse response = new GoogleLoginResponse(
                            accessToken, refreshToken, newUser.getUserId(), true, true
                    );
                    return new GoogleLoginResult(response, true);
                });
    }

    @Transactional
    public void logout(Long userId, LogoutRequest request) {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다."));

        if (!tokenEntity.getUserId().equals(userId)) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다.");
        }

        tokenEntity.setIsRevoked(true);
        refreshTokenRepository.save(tokenEntity);
    }

    @Transactional
    public void withdraw(Long userId, WithdrawRequest request) {
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다."));

        if (!tokenEntity.getUserId().equals(userId)) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "유효하지 않은 Refresh Token입니다.");
        }

        tokenEntity.setIsRevoked(true);
        refreshTokenRepository.save(tokenEntity);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "존재하지 않는 계정입니다."));
        user.setStatus("DELETED");
        user.setDeletedAt(LocalDateTime.now());
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail() + "_deleted_" + System.currentTimeMillis());
        }
        userRepository.save(user);

        List<Long> buttonIds = buttonRepository.findActiveButtonIdsByUserId(userId);
        if (!buttonIds.isEmpty()) {
            buttonRecordRepository.softDeleteByButtonIds(buttonIds);
            reminderRepository.softDeleteByButtonIds(buttonIds);
        }
        buttonRepository.deactivateAllByUserId(userId);
    }

    private boolean resolveOnboardingRequired(Long userId) {
        return userTemplateSelectionRepository
                .findByUserId(userId)
                .map(selection -> !Boolean.TRUE.equals(selection.getIsCompleted()))
                .orElse(true);
    }

    private void saveRefreshToken(Long userId, String refreshToken) {
        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setUserId(userId);
        tokenEntity.setToken(refreshToken);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusSeconds(jwtProvider.getRefreshTokenExpiration() / 1000));
        tokenEntity.setIsRevoked(false);
        refreshTokenRepository.save(tokenEntity);
    }

    private String generateDefaultUsername(String email) {
        return email.split("@")[0];
    }

    public record GoogleLoginResult(GoogleLoginResponse response, boolean isNewUser) {
    }
}