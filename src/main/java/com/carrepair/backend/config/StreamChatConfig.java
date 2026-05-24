package com.carrepair.backend.config;

import io.getstream.chat.java.services.framework.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StreamChatConfig {

    @Value("${stream.api.key}")
    private String apiKey;

    @Value("${stream.api.secret}")
    private String apiSecret;

    @Bean
    public Client streamChatClient() {
        System.setProperty("STREAM_KEY", apiKey);
        System.setProperty("STREAM_SECRET", apiSecret);
        return Client.getInstance();
    }
}
