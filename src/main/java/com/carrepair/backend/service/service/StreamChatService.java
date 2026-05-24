package com.carrepair.backend.service.service;


import io.getstream.chat.java.models.Channel;
import io.getstream.chat.java.models.User;
import io.getstream.chat.java.services.framework.Client;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamChatService {

    private final Client streamChatClient;

    @Value("${stream.api.secret}")
    private String streamApiSecret;

    public String generateUserToken(Long userId) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(streamApiSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.builder()
                    .claim("user_id", userId.toString())
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            log.error("Failed to generate Stream token for userId {}: {}", userId, e.getMessage());
            throw new RuntimeException("Could not generate Stream chat token");
        }
    }

    public void createOrUpdateStreamUser(Long userId, String fullName, String email, String role) {
        try {
            User.upsert()
                    .user(User.UserRequestObject.builder()
                            .id(userId.toString())
                            .name(fullName)
                            .additionalField("email", email)
                            .additionalField("role", role)
                            .build())
                    .request();
        } catch (Exception e) {
            log.error("Failed to upsert Stream user for userId {}: {}", userId, e.getMessage());
        }
    }


    public String createChannel(Long leadId, Long carOwnerId, Long repairShopId, String channelName) {
        try {
            Channel.getOrCreate("messaging", "lead-" + leadId)
                    .data(Channel.ChannelRequestObject.builder()
                            .member(Channel.ChannelMemberRequestObject.builder()
                                    .userId(carOwnerId.toString())
                                    .build())
                            .member(Channel.ChannelMemberRequestObject.builder()
                                    .userId(repairShopId.toString())
                                    .build())
                            .additionalField("leadId", leadId.toString())
                            .additionalField("channelName", channelName)
                            .build())
                    .request();

            return "messaging:lead-" + leadId;
        } catch (Exception e) {
            log.error("Failed to create Stream channel for leadId {}: {}", leadId, e.getMessage());
            throw new RuntimeException("Could not create Stream chat channel");
        }
    }
}