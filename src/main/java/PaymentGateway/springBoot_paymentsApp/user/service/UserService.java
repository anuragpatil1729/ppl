package PaymentGateway.springBoot_paymentsApp.user.service;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import PaymentGateway.springBoot_paymentsApp.user.dto.LoginRequest;
import PaymentGateway.springBoot_paymentsApp.user.dto.RegisterRequest;
import PaymentGateway.springBoot_paymentsApp.user.entity.UserEntity;
import PaymentGateway.springBoot_paymentsApp.user.repo.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        return userRepository.save(user);
    }

    public Optional<UserEntity> login(LoginRequest request) {
        return userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()));
    }
}
