package com.jacksearle.backend.config;

import com.jacksearle.backend.board.BoardMemberRepository;
import com.jacksearle.backend.security.JwtService;
import com.jacksearle.backend.user.User;
import com.jacksearle.backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final String allowedOriginPatterns;
    private final String frontendUrl;

    public WebSocketConfig(
            JwtService jwtService,
            UserRepository userRepository,
            BoardMemberRepository boardMemberRepository,
            @Value("${app.websocket.allowed-origin-patterns:}") String allowedOriginPatterns,
            @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.boardMemberRepository = boardMemberRepository;
        this.allowedOriginPatterns = allowedOriginPatterns;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(parseAllowedOriginPatterns())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) {
                    return message;
                }
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    authenticate(accessor);
                }
                if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    authorizeSubscription(accessor);
                }
                return message;
            }
        });
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing websocket authorization token");
        }
        String email = jwtService.extractUsername(header.substring(7));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid websocket user"));
        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("Email verification required");
        }
        accessor.setUser(new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }
        if (accessor.getUser() == null) {
            throw new IllegalArgumentException("Websocket authentication is required");
        }
        if (destination.startsWith("/topic/users/")) {
            authorizeUserSubscription(accessor, destination);
            return;
        }
        if (!destination.startsWith("/topic/boards/")) {
            return;
        }
        Long boardId = Long.valueOf(destination.substring("/topic/boards/".length()));
        String email = accessor.getUser().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid websocket user"));
        if (boardMemberRepository.findByBoardIdAndUserId(boardId, user.getId()).isEmpty()) {
            throw new IllegalArgumentException("You do not have access to this board");
        }
    }

    private void authorizeUserSubscription(StompHeaderAccessor accessor, String destination) {
        String suffix = destination.substring("/topic/users/".length());
        String userId = suffix.replaceFirst("/.*$", "");
        String email = accessor.getUser().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid websocket user"));
        if (!String.valueOf(user.getId()).equals(userId)) {
            throw new IllegalArgumentException("You do not have access to this user topic");
        }
    }

    private String[] parseAllowedOriginPatterns() {
        String origins = String.join(",",
                "http://localhost:*",
                "http://127.0.0.1:*",
                frontendUrl == null ? "" : frontendUrl,
                allowedOriginPatterns == null ? "" : allowedOriginPatterns
        );

        return List.of(origins.split(","))
                .stream()
                .map(String::trim)
                .filter(pattern -> !pattern.isBlank())
                .toArray(String[]::new);
    }
}
