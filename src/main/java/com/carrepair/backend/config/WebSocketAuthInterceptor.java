package com.carrepair.backend.config;


import com.carrepair.backend.service.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    public WebSocketAuthInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authorizationHeaders =
                    accessor.getNativeHeader("Authorization");

            if (authorizationHeaders == null || authorizationHeaders.isEmpty()) {
                throw new MessageDeliveryException("Unauthorized");
            }

            String authHeader = authorizationHeaders.get(0);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessageDeliveryException("Unauthorized");
            }

            String token = authHeader.substring(7);

            if (!jwtService.isTokenValid(token)) {
                throw new MessageDeliveryException("Unauthorized");
            }

            String email = jwtService.extractEmail(token);

            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                sessionAttributes.put("email", email);
            }
        }

        return message;
    }
}
