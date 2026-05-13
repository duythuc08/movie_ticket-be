package com.example.movie_ticket_be.auth.service;

import com.example.movie_ticket_be.auth.dto.request.AuthenticationResquest;
import com.example.movie_ticket_be.auth.dto.request.IntrospectResquest;
import com.example.movie_ticket_be.auth.dto.request.LogoutResquest;
import com.example.movie_ticket_be.auth.dto.request.RegisterRequest;
import com.example.movie_ticket_be.auth.dto.response.AuthenticationResponse;
import com.example.movie_ticket_be.auth.dto.response.IntrospectResponse;
import com.example.movie_ticket_be.auth.entity.InvalidatedToken;
import com.example.movie_ticket_be.auth.entity.VerificationToken;
import com.example.movie_ticket_be.auth.repository.InvalidatedTokenRepository;
import com.example.movie_ticket_be.auth.repository.VerificationTokenRepository;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.user.dto.response.UsersRespone;
import com.example.movie_ticket_be.user.entity.MembershipTier;
import com.example.movie_ticket_be.user.entity.Role;
import com.example.movie_ticket_be.user.entity.Users;
import com.example.movie_ticket_be.user.enums.Roles;
import com.example.movie_ticket_be.user.enums.UserStatus;
import com.example.movie_ticket_be.user.mapper.UserMapper;
import com.example.movie_ticket_be.user.repository.MembershipTierRepository;
import com.example.movie_ticket_be.user.repository.RoleRepository;
import com.example.movie_ticket_be.user.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    private final UserMapper userMapper;
    private final VerificationTokenRepository verificationTokenRepository;
    private final @Lazy EmailService emailService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MembershipTierRepository membershipTierRepository;
    @NonFinal
    @Value("${jwt.signerKey}") //doc bien tu file .yaml
    protected String SIGNER_KEY;

    public AuthenticationResponse authenticate(AuthenticationResquest resquest) {
        var user = userRepository.findByUsername(resquest.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated =  passwordEncoder.matches(resquest.getPassword(), user.getPassword());

        if(!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .enabled(user.isEnabled())
                .build();
    }

    public void logout(LogoutResquest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken());

            String jit = signToken.getJWTClaimsSet().getJWTID();

            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jit)
                    .expiryTime(expiryTime)
                    .build();

            invalidatedTokenRepository.save(invalidatedToken);

        } catch (AppException exception) {
            log.info("Token already expired");
        }
    }


    @Transactional
    public UsersRespone register(RegisterRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        if(userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        MembershipTier defaultTier = membershipTierRepository.findByName("MEMBER")
                .orElseThrow(() -> new RuntimeException("Lỗi hệ thống: Chưa cấu hình hạng thành viên mặc định"));
        Role role = roleRepository.save(Role.builder().name(Roles.USER.name()).build());
        var roles = new HashSet<Role>();
        roles.add(role);

        Users user = userMapper.toRegisterUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(roles);
        user.setUserStatus(UserStatus.ACTIVE);
        user.setEnabled(false); // mặc định chưa verify
        user.setLoyaltyPoints(0);
        user.setMembershipTier(defaultTier);
        userRepository.save(user);

        String otp = generateVerificationCode();
        VerificationToken token = new VerificationToken();
        token.setUser(user);
        token.setVerificationCode(otp);
        token.setVerificationCodeExpiresAt(LocalDateTime.now().plusSeconds(60));
        verificationTokenRepository.save(token);

        sendMail(user,token.getVerificationCode());
        return userMapper.toUsersRespone(user);
    }

    @Transactional
    public void forgotPassword(String email) {
        Users user = userRepository.findByUsername(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String otp = generateVerificationCode();
        VerificationToken token = verificationTokenRepository.findByUser(user)
                .orElse(new VerificationToken());

        token.setUser(user);
        token.setVerificationCode(otp);
        token.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setInvalidated(false);
        token.setLastSentAt(LocalDateTime.now());

        verificationTokenRepository.save(token);
        verificationTokenRepository.flush();
        emailService.sendEmail(user.getUsername(), "Reset Password OTP", otp);
    }


    @Transactional
    public void resetPassword(String email, String newPassword) {
        Users user = userRepository.findByUsername(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        VerificationToken token = verificationTokenRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_VERIFIED));

        if (token.isInvalidated()) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } else {
            throw new AppException(ErrorCode.OTP_NOT_VERIFIED);
        }

    }


    public IntrospectResponse introspect(IntrospectResquest resquest)
            throws JOSEException, ParseException {

        var token = resquest.getToken();
        boolean inValid = true;
        try {
            verifyToken(token);
        } catch (AppException e) {
            inValid = false;
        }

        return IntrospectResponse.builder()
                .valid(inValid)
                .build();
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expityTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);
        if (!(verified && expityTime.after(new Date())))
            throw new AppException(ErrorCode.AUTHENTICATED);

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
            throw new AppException(ErrorCode.AUTHENTICATED);

        //Trả về IntrospectRespone: true nếu:
        //        (a) chữ ký hợp lệ
        //        (b) token chưa hết hạn
        return signedJWT;
    }

    private String generateToken(Users user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("infinity_cinema.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now()
                                .plus(30, ChronoUnit.MINUTES)
                                .toEpochMilli()
                ))
                .claim("userId", user.getUserId())
                .claim("scope", buildScope(user))
                .jwtID(UUID.randomUUID().toString())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject  jwsObject = new JWSObject(jwsHeader,payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildScope(Users user) {
        StringJoiner scope = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRole()))
            user.getRole().forEach(role -> {
                scope.add(role.getName());
            });

        return scope.toString();
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000);
        return String.valueOf(code);
    }

    @Transactional
    public void verifyEmail(String otp, String userName) {
        Users user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        VerificationToken verificationToken = verificationTokenRepository
                .findByUserAndVerificationCode(user, otp)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_OTP));

        if (verificationToken.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        if (verificationToken.isInvalidated()) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        if (!user.isEnabled()) {
            user.setEnabled(true);
            userRepository.save(user);
        }

        verificationToken.setInvalidated(true);
        verificationTokenRepository.save(verificationToken);
    }


    @Transactional
    public void resendOtp(String userName) {
        Users user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        VerificationToken token = verificationTokenRepository.findByUser(user)
                .orElseThrow(() -> new AppException(ErrorCode.OTP_NOT_FOUND));

        if (token.getLastSentAt() != null &&
                token.getLastSentAt().isAfter(LocalDateTime.now().minusSeconds(30))) {
            throw new AppException(ErrorCode.OTP_RESEND_TOO_SOON);
        }

        String otp = generateVerificationCode();
        token.setVerificationCode(otp);
        token.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setInvalidated(false);
        token.setLastSentAt(LocalDateTime.now());
        verificationTokenRepository.save(token);

        //Send mail
        sendMail(user,token.getVerificationCode());
    }

    private void sendMail(Users user,String otp){
        //Send Email
        String verificationCode = "VERIFICATION CODE " + otp;
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            emailService.sendEmail(user.getUsername(), "Xác thực tài khoản", htmlMessage);
            log.info("Email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send email", e);
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void deleteUnverifiedUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(5);
        List<Users> unverifiedUsers = userRepository.findAllByEnabledFalseAndCreatedAtBefore(cutoff);

        if (unverifiedUsers.isEmpty()) {
            log.info("No unverified users to delete.");
            return;
        }

        for (Users user : unverifiedUsers) {
            try {
                log.info("Deleting unverified user: {}", user.getUsername());
                verificationTokenRepository.deleteByUser(user);
                user.getRole().clear();
                userRepository.save(user);
                userRepository.delete(user);
            } catch (Exception e) {
                log.error("Error deleting user {}: {}", user.getUsername(), e.getMessage());
            }
        }
    }
}
