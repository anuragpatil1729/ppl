package PaymentGateway.springBoot_paymentsApp.user.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import PaymentGateway.springBoot_paymentsApp.user.dto.LoginRequest;
import PaymentGateway.springBoot_paymentsApp.user.dto.RegisterRequest;
import PaymentGateway.springBoot_paymentsApp.user.entity.UserEntity;
import PaymentGateway.springBoot_paymentsApp.user.repo.UserRepository;

@Service
public class UserService {

    public enum OtpVerificationStatus {
        VERIFIED,
        INVALID,
        EXPIRED,
        USER_NOT_FOUND
    }

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public UserEntity register(RegisterRequest request) {
        UserEntity user = new UserEntity();
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setMobile(request.getMobile());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerified(false);
        refreshOtp(user);
        UserEntity savedUser = userRepository.save(user);
        emailService.sendOtpEmail(savedUser.getEmail(), savedUser.getOtp());
        return savedUser;
    }

    public Optional<UserEntity> login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()));
    }

    public OtpVerificationStatus verifyOtp(String email, String otp) {
        Optional<UserEntity> optionalUser = userRepository.findByEmail(normalizeEmail(email));
        if (optionalUser.isEmpty()) {
            return OtpVerificationStatus.USER_NOT_FOUND;
        }

        UserEntity user = optionalUser.get();
        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            return OtpVerificationStatus.EXPIRED;
        }

        if (user.getOtp() == null || !user.getOtp().equals(otp)) {
            return OtpVerificationStatus.INVALID;
        }

        user.setVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
        return OtpVerificationStatus.VERIFIED;
    }

    public boolean resendOtp(String email) {
        Optional<UserEntity> optionalUser = userRepository.findByEmail(normalizeEmail(email));
        if (optionalUser.isEmpty()) {
            return false;
        }

        UserEntity user = optionalUser.get();
        refreshOtp(user);
        userRepository.save(user);
        emailService.sendOtpEmail(user.getEmail(), user.getOtp());
        return true;
    }

    public Optional<UserEntity> findById(Long id) {
        return userRepository.findById(id);
    }

    private void refreshOtp(UserEntity user) {
        user.setOtp(generateOtp());
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
    }

    private String generateOtp() {
        int otp = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(otp);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
