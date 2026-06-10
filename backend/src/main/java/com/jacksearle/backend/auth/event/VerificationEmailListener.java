package com.jacksearle.backend.auth.event;

import com.jacksearle.backend.auth.VerificationEmailService;
import com.jacksearle.backend.user.User;
import com.jacksearle.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationEmailListener {
    private final UserRepository userRepository;
    private final VerificationEmailService verificationEmailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserRegisteredEvent event) {
        User user = userRepository.findById(event.userId()).orElseThrow();
        try {
            verificationEmailService.sendVerificationEmail(user);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}", user.getEmail(), e);
        }
    }
}
