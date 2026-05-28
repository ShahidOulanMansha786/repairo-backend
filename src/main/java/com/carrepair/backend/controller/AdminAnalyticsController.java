package com.carrepair.backend.controller;

import com.carrepair.backend.dto.response.analytics.AnalyticsResponseDTO;
import com.carrepair.backend.dto.response.analytics.TrendPointDTO;
import com.carrepair.backend.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<AnalyticsResponseDTO> getAnalytics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        validateParams(period, startDate, endDate);
        return ResponseEntity.ok(analyticsService.getAnalytics(period, startDate, endDate));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        validateParams(period, startDate, endDate);
        List<TrendPointDTO> trend = analyticsService
                .getAnalytics(period, startDate, endDate).getTrend();
        byte[] csv = buildCsv(trend, period);

        String filename = "analytics-trend-" + period.toLowerCase() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    private void validateParams(String period, String startDate, String endDate) {
        boolean hasPeriod = period != null;
        boolean hasCustom = startDate != null && endDate != null;

        if (!hasPeriod && !hasCustom) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Either period or startDate+endDate required"
            );
        }
        if (hasPeriod && !List.of("7D","30D","12M").contains(period.toUpperCase())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid period. Allowed: 7D, 30D, 12M"
            );
        }
        if (hasCustom) {
            try {
                LocalDate.parse(startDate);
                LocalDate.parse(endDate);
            } catch (Exception e) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid date format. Use YYYY-MM-DD"
                );
            }
        }
    }

    private void validatePeriod(String period) {
        List<String> valid = List.of("7D", "30D", "12M");
        if (!valid.contains(period.toUpperCase())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid period. Allowed: 7D, 30D, 12M"
            );
        }
    }

    private byte[] buildCsv(List<TrendPointDTO> trend, String period) {
        StringBuilder sb = new StringBuilder();

        sb.append("Period,").append(period.toUpperCase()).append("\n");
        sb.append("Date,Leads Count\n");

        for (TrendPointDTO point : trend) {
            sb.append(point.getDate())
                    .append(",")
                    .append(point.getCount())
                    .append("\n");
        }

        long total = trend.stream()
                .mapToLong(TrendPointDTO::getCount)
                .sum();
        sb.append("TOTAL,").append(total).append("\n");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
