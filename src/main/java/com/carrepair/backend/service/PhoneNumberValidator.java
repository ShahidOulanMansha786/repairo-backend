package com.carrepair.backend.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

// PhoneNumberValidator.java
@Service
public class PhoneNumberValidator {

    // Valid PK mobile prefixes: 030x, 031x, 032x, 033x, 034x, 035x
    private static final Pattern PK_MOBILE_PATTERN =
            Pattern.compile("^03[0-5][0-9]{8}$");

    public boolean isValidPakistaniNumber(String phoneNumber) {
        if (phoneNumber == null) return false;

        // Strip spaces, dashes, +92 prefix if present
        String normalized = normalizeNumber(phoneNumber);

        return PK_MOBILE_PATTERN.matcher(normalized).matches();
    }

    private String normalizeNumber(String phone) {
        // Remove spaces, dashes, dots
        phone = phone.replaceAll("[\\s\\-\\.]", "");

        // Convert +92XXXXXXXXXX → 0XXXXXXXXXX
        if (phone.startsWith("+92")) {
            phone = "0" + phone.substring(3);
        }
        // Convert 92XXXXXXXXXX → 0XXXXXXXXXX
        else if (phone.startsWith("92") && phone.length() == 12) {
            phone = "0" + phone.substring(2);
        }

        return phone;
    }
}
