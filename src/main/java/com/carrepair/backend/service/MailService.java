package com.carrepair.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendOtpEmail(String toEmail, String otp) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Your OTP Code");
            helper.setText(buildOtpEmailBody(otp), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send OTP email to: " + toEmail, e);
        }
    }

    private String buildOtpEmailBody(String otp) {
        return """
                <div style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2>Your OTP Code</h2>
                    <p>Use the following OTP to verify your account:</p>
                    <h1 style="color: #4A90E2; letter-spacing: 4px;">%s</h1>
                    <p>This code expires in 5 minutes. Do not share it with anyone.</p>
                </div>
                """.formatted(otp);
    }
}