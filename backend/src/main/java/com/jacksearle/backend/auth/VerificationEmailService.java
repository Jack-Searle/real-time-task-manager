package com.jacksearle.backend.auth;

import com.jacksearle.backend.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class VerificationEmailService {
    private static final Logger log = LoggerFactory.getLogger(VerificationEmailService.class);

    private final JavaMailSender mailSender;
    private final String frontendUrl;
    private final String fromAddress;

    public VerificationEmailService(
            JavaMailSender mailSender,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.mail.from}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
        this.fromAddress = fromAddress;
    }

    public void sendVerificationEmail(User user) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + user.getVerificationToken();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(user.getEmail());
        message.setSubject("Verify your Task Manager account");
        message.setText("""
                Welcome to Task Manager.

                Verify your email address by opening this link:
                %s

                This link expires in 24 hours.
                """.formatted(verificationUrl));

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send verification email to {}", user.getEmail(), ex);
            throw ex;
        }
    }
}
