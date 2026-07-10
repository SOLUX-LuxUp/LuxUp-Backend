package com.taptap.backend.user.service;

import com.taptap.backend.config.AuthException;
import com.taptap.backend.user.dto.VerificationCodeResponse;
import com.taptap.backend.user.entity.EmailVerification;
import com.taptap.backend.user.repository.EmailVerificationRepository;
import com.taptap.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class EmailVerificationService {

    private static final int EXPIRE_SECONDS = 300;

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    public EmailVerificationService(
            EmailVerificationRepository emailVerificationRepository,
            UserRepository userRepository,
            JavaMailSender mailSender
    ) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    @Transactional
    public VerificationCodeResponse sendCode(String email) {
        if (userRepository.existsByEmailAndStatus(email, "ACTIVE")) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "이미 가입된 이메일입니다.");
        }

        String code = generateCode();

        EmailVerification verification = new EmailVerification();
        verification.setEmail(email);
        verification.setCode(code);
        verification.setExpiresAt(LocalDateTime.now().plusSeconds(EXPIRE_SECONDS));
        verification.setIsUsed(false);

        emailVerificationRepository.save(verification);

        sendMail(email, code);

        return new VerificationCodeResponse(EXPIRE_SECONDS);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1_000_000));
    }

    private void sendMail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[TapTap] 이메일 인증코드");
        message.setText("인증코드: " + code + " (5분 이내 입력해주세요.)");
        mailSender.send(message);
    }
}