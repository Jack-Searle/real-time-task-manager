package com.jacksearle.backend.auth;

import com.jacksearle.backend.auth.dto.AuthResponse;
import com.jacksearle.backend.auth.dto.RegisterRequest;
import com.jacksearle.backend.auth.dto.RegisterResponse;
import com.jacksearle.backend.common.exception.BadRequestException;
import com.jacksearle.backend.security.JwtService;
import com.jacksearle.backend.user.Role;
import com.jacksearle.backend.user.User;
import com.jacksearle.backend.user.UserRepository;
import com.jacksearle.backend.user.UserResponse;
import com.jacksearle.backend.auth.event.UserRegisteredEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BadRequestException("Email already exists");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                encodedPassword,
                Role.USER
        );
        assignVerificationToken(user);
        user = userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId()));
        return new RegisterResponse("Registration successful. Please verify your email before logging in.", toUserResponse(user));
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Verification link is invalid"));
        if (user.isEmailVerified()) {
            return;
        }
        if (user.getVerificationTokenExpiry() == null || user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification link has expired");
        }
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Transactional
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Account not found"));
        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }
        assignVerificationToken(user);
        userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(user.getId()));
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }
        if (!user.isEmailVerified()) {
            throw new BadRequestException("Please verify your email before logging in");
        }
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, toUserResponse(user));
    }

    private void assignVerificationToken(User user) {
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isEmailVerified()
        );
    }
}
