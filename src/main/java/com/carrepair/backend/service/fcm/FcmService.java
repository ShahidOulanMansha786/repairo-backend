package com.carrepair.backend.service.fcm;


import com.carrepair.backend.entity.Lead;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FirebaseApp firebaseApp;

    public void sendNotification(String fcmToken, String title, String body, Map<String, String> data) {

        log.info("FCM sendNotification called. Token: {}, Title: {}", fcmToken, title);

        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data)
                    .build();

            FirebaseMessaging.getInstance().send(message);

        } catch (FirebaseMessagingException e) {
            log.error("FCM error code: {}", e.getMessagingErrorCode());
            log.error("FCM error message: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to send FCM notification to token {}: {}", fcmToken, e.getMessage());
        }
    }

    public void sendShopApprovedNotification(String fcmToken, String shopName) {
        sendNotification(
                fcmToken,
                "Application Approved",
                "Congratulations! " + shopName + " has been approved.",
                Map.of("type", "SHOP_APPROVED")
        );
    }

    public void sendShopRejectedNotification(String fcmToken, String shopName, String reason) {
        sendNotification(
                fcmToken,
                "Application Rejected",
                "Unfortunately, " + shopName + " has not been approved.",
                Map.of("type", "SHOP_REJECTED", "reason", reason)
        );
    }

    public void sendNewLeadNotification(String fcmToken, Lead lead) {

        sendNotification(
                fcmToken,
                "New Lead Near You",
                lead.getTitle() + " — " + lead.getCarMake() + " " + lead.getCarModel(),
                Map.of("type", "NEW_LEAD", "leadId", lead.getId().toString())
        );
    }

    public void sendQuoteAcceptedNotification(String fcmToken, String shopName, String leadTitle) {

        sendNotification(
                fcmToken,
                "Quote Accepted!",
                "Your quote for '" + leadTitle + "' has been accepted",
                Map.of("type", "QUOTE_ACCEPTED")
        );
    }

    public void sendQuoteRejectedNotification(String fcmToken, String shopName, String leadTitle) {
        sendNotification(
                fcmToken,
                "Quote Not Selected",
                "Your quote for '" + leadTitle + "' was not selected",
                Map.of("type", "QUOTE_REJECTED")
        );
    }

    public void sendChatMessageNotification(String fcmToken, String senderName, String messagePreview, String channelId) {
        sendNotification(
                fcmToken,
                senderName,
                messagePreview,
                Map.of("type", "NEW_MESSAGE", "channelId", channelId)
        );
    }

    public void sendAccountBlockedNotification(String fcmToken) {
        sendNotification(fcmToken, "Account Blocked",
                "Your account has been blocked. Please contact support.",
                Map.of("type", "ACCOUNT_BLOCKED"));
    }

    public void sendAccountUnblockedNotification(String fcmToken) {
        sendNotification(fcmToken, "Account Unblocked",
                "Your account has been unblocked. You can now use the app.",
                Map.of("type", "ACCOUNT_UNBLOCKED"));
    }

    public void sendShopMarkedDoneNotification(String token, String leadTitle) {
        sendNotification(
                token,
                "Shop Has Finished Work",
                leadTitle + " is ready for review",
                Map.of("type", "SHOP_MARKED_DONE")
        );
    }

    public void sendJobCompletedNotification(String token, String leadTitle) {
        sendNotification(
                token,
                "Job Completed!",
                leadTitle + " has been completed",
                Map.of("type", "JOB_COMPLETED")
        );
    }

    public void sendDisputeRaisedNotification(String token, String leadTitle) {
        sendNotification(
                token,
                "Dispute Raised",
                "A dispute has been raised for: " + leadTitle,
                Map.of("type", "DISPUTE_RAISED")
        );
    }

    public void sendDisputeResolvedNotification(String token, String resolution) {
        String body = resolution.equals("RESOLVED_SHOP")
                ? "Decision: Payment released to shop"
                : "Decision: Refund issued to owner";

        sendNotification(
                token,
                "Dispute Resolved",
                body,
                Map.of("type", "DISPUTE_RESOLVED")
        );
    }
}
