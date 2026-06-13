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
    private final String backendUrl;
    private final String fromAddress;

    public VerificationEmailService(
            RestClient.Builder restClientBuilder,
            @Value("${resend.api-key:}") String resendApiKey,
            @Value("${app.backend-url}") String backendUrl,
            @Value("${app.mail.from}") String fromAddress
    ) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.resend.com")
                .defaultHeader(HttpHeaders.USER_AGENT, "real-time-task-manager/1.0")
                .build();
        this.resendApiKey = resendApiKey;
        this.backendUrl = backendUrl;
        this.fromAddress = fromAddress;
    }

    public void sendVerificationEmail(User user) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            throw new IllegalStateException("RESEND_API_KEY must be configured before verification emails can be sent");
        }

        String verificationUrl = backendUrl.replaceAll("/+$", "") + "/api/auth/verify-email/redirect?token=" + user.getVerificationToken();
        String firstName = user.getFirstName() == null || user.getFirstName().isBlank() ? "there" : user.getFirstName();
        Map<String, Object> payload = Map.of(
                "from", normalizeFromAddress(fromAddress),
                "to", List.of(user.getEmail()),
                "subject", "Verify your Real-Time Task Manager email address",
                "text", """
                Hi %s,

                Thanks for creating a Real-Time Task Manager account.

                Confirm your email address by opening this link:
                %s

                This link expires in 24 hours.
                If you did not create this account, you can ignore this email.
                """.formatted(firstName, verificationUrl),
                "html", buildVerificationEmailHtml(firstName, verificationUrl),
                "headers", Map.of(
                        "List-Unsubscribe", "<mailto:" + normalizeFromAddress(fromAddress) + "?subject=unsubscribe>"
                )
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

    private String normalizeFromAddress(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim();
        if ((normalized.startsWith("\"") && normalized.endsWith("\""))
                || (normalized.startsWith("'") && normalized.endsWith("'"))) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }

        int start = normalized.indexOf('<');
        int end = normalized.indexOf('>');
        if (start >= 0 && end > start) {
            return normalized.substring(start + 1, end).trim();
        }

        return normalized;
    }

    private String buildVerificationEmailHtml(String firstName, String verificationUrl) {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Verify your email</title>
                </head>
                <body style="margin:0;padding:0;background:#f4f7fb;font-family:Arial,Helvetica,sans-serif;color:#172033;">
                    <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="background:#f4f7fb;margin:0;padding:32px 16px;">
                        <tr>
                            <td align="center">
                                <table role="presentation" width="100%%" cellspacing="0" cellpadding="0" style="max-width:560px;background:#ffffff;border-radius:16px;overflow:hidden;border:1px solid #e4ebf5;">
                                    <tr>
                                        <td style="background:#0f1724;padding:28px 32px;">
                                            <div style="font-size:14px;letter-spacing:.08em;text-transform:uppercase;color:#78bfff;font-weight:700;">Real-Time Task Manager</div>
                                            <h1 style="margin:10px 0 0;color:#ffffff;font-size:26px;line-height:1.25;font-weight:800;">Verify your email address</h1>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="padding:32px;">
                                            <p style="margin:0 0 16px;font-size:16px;line-height:1.6;color:#263349;">Hi %s,</p>
                                            <p style="margin:0 0 20px;font-size:16px;line-height:1.6;color:#263349;">Thanks for creating a Real-Time Task Manager account. Confirm your email address to finish setting up your account and start managing your boards.</p>
                                            <table role="presentation" cellspacing="0" cellpadding="0" style="margin:28px 0;">
                                                <tr>
                                                    <td style="border-radius:10px;background:#3298ff;">
                                                        <a href="%s" style="display:inline-block;padding:14px 22px;color:#06111f;text-decoration:none;font-size:16px;font-weight:800;border-radius:10px;">Verify email address</a>
                                                    </td>
                                                </tr>
                                            </table>
                                            <p style="margin:0 0 10px;font-size:14px;line-height:1.6;color:#66748a;">This link expires in 24 hours.</p>
                                            <p style="margin:0 0 8px;font-size:14px;line-height:1.6;color:#66748a;">If the button does not work, copy and paste this link into your browser:</p>
                                            <p style="margin:0;word-break:break-all;font-size:14px;line-height:1.6;"><a href="%s" style="color:#1976d2;text-decoration:underline;">%s</a></p>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td style="border-top:1px solid #e4ebf5;padding:20px 32px;background:#fbfcfe;">
                                            <p style="margin:0;font-size:13px;line-height:1.6;color:#7b8798;">If you did not create this account, you can safely ignore this email.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(firstName, verificationUrl, verificationUrl, verificationUrl);
    }
}
