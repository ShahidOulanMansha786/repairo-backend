package com.carrepair.backend.service.chat;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class FirestoreChatService {

    public String createChatChannel(Long leadId,
                                    Long carOwnerId,
                                    String carOwnerName,
                                    Long repairShopId,
                                    String repairShopName,
                                    String leadTitle) {
        try {
            Firestore db = FirestoreClient.getFirestore("defaultcarrepair");

            Map<String, Object> data = new HashMap<>();
            data.put("channelId", "lead_" + leadId);
            data.put("leadId", leadId.toString());
            data.put("carOwnerId", carOwnerId.toString());
            data.put("carOwnerName", carOwnerName);
            data.put("repairShopId", repairShopId.toString());
            data.put("repairShopName", repairShopName);
            data.put("leadTitle", leadTitle);
            data.put("createdAt", new Date());
            data.put("lastMessage", null);
            data.put("lastMessageAt", null);
            data.put("lastMessageSenderId", null);

            db.collection("chats")
                    .document("lead_" + leadId)
                    .set(data)
                    .get();

            return "lead_" + leadId;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create Firestore chat channel for lead " + leadId, e);
        }
    }
}
