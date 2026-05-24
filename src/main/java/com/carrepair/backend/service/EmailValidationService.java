package com.carrepair.backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

@Service
public class EmailValidationService {

    private final Set<String> blockedDomains = new HashSet<>();

    @PostConstruct
    public void loadBlocklist() {
        try (InputStream is = getClass().getResourceAsStream("/disposable_email_blocklist.conf");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .forEach(blockedDomains::add);

        } catch (IOException e) {
            System.err.println("Could not load blocklist: " + e.getMessage());
        }
    }

    public boolean isDisposable(String email) {
        if (email == null || !email.contains("@")) return false;
        String domain = email.substring(email.lastIndexOf('@') + 1).toLowerCase();
        return blockedDomains.contains(domain);
    }
}
