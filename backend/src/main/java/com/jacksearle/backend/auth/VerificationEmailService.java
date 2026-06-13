package com.jacksearle.backend.auth;

import com.jacksearle.backend.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class VerificationEmailService {
    private static final Logger log = LoggerFactory.getLogger(VerificationEmailService.class);

    private final RestClient restClient;
    private final String resendApiKey;
    private final String frontendUrl;
    private final String fromAddress;

    public VerificationEmailService(
            RestClient.Builder restClientBuilder,
            @Value("${resend.api-key:}") String resendApiKey,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.mail.from}") String fromAddress
    ) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.resend.com")
                .defaultHeader(HttpHeaders.USER_AGENT, "real-time-task-manager/1.0")
                .build();
        this.resendApiKey = resendApiKey;
        this.frontendUrl = frontendUrl;
        this.fromAddress = fromAddress;
    }

    public void sendVerificationEmail(User user) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            throw new IllegalStateException("RESEND_API_KEY must be configured before verification emails can be sent");
        }

        String verificationUrl = frontendUrl.replaceAll("/+$", "") + "/verify-email?token=" + user.getVerificationToken();
        Map<String, Object> payload = Map.of(
                "from", fromAddress,
                "to", List.of(user.getEmail()),
                "subject", "Verify your Task Manager account",
                "text", """
                Welcome to Task Manager.

                Verify your email address by opening this link:
                %s

                This link expires in 24 hours.
                """.formatted(verificationUrl),
                "html", """
                <p>Welcome to Task Manager.</p>
                <p>Verify your email address by opening this link:</p>
                <p><a href="%s">Verify your email</a></p>
                <p>This link expires in 24 hours.</p>
                """.formatted(verificationUrl)
        );

        try {
            restClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            log.error("Resend failed to send verification email to {}. Status: {}. Response: {}",
                    user.getEmail(),
                    ex.getStatusCode(),
                    ex.getResponseBodyAsString(),
                    ex);
            throw ex;
        }
    }
}
