package com.carrepair.backend.schedular;


import com.carrepair.backend.entity.Lead;
import com.carrepair.backend.entity.LeadStatus;
import com.carrepair.backend.repository.LeadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeadExpiryJob {

    private final LeadRepository leadRepository;

    @Scheduled(fixedRate = 300000)
    public void expireOldLeads() {
        List<Lead> expiredLeads = leadRepository.findByStatusAndExpiresAtBefore(
                LeadStatus.OPEN, LocalDateTime.now());

        for (Lead lead : expiredLeads) {
            lead.setStatus(LeadStatus.CANCELLED);
            leadRepository.save(lead);
        }

        log.info("Expired {} leads", expiredLeads.size());
    }
}
