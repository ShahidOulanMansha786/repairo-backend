package com.carrepair.backend.service;

import com.carrepair.backend.dto.response.analytics.*;
import com.carrepair.backend.repository.LeadRepository;
import com.carrepair.backend.repository.QuoteRepository;
import com.carrepair.backend.repository.RepairShopRepository;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final LeadRepository leadRepository;
    private final RepairShopRepository repairShopRepository;
    private final QuoteRepository quoteRepository;

    private static final double GMV_PER_LEAD = 50.0;
    private static final double GMV_CHANGE_HARDCODED = 8.2;

    private static final Map<Long, Double> HARDCODED_RATINGS = Map.of(
            1L, 4.9, 2L, 4.7, 3L, 4.5, 4L, 4.3, 5L, 4.1
    );
    private static final double DEFAULT_RATING = 4.0;

    public AnalyticsResponseDTO getAnalytics(
            String period, String startDate, String endDate) {
        DateRange current = getDateRange(period, startDate, endDate);
        DateRange previous = getPreviousDateRange(period, startDate, endDate);

        return AnalyticsResponseDTO.builder()
                .stats(buildStats(current, previous))
                .trend(buildTrend(current, period, startDate, endDate))
                .statusDistribution(buildStatusDistribution(current))
                .topShops(buildTopShops(current))
                .build();
    }

    private AnalyticsStatsDTO buildStats(DateRange current, DateRange previous) {

        long currLeads = leadRepository.countByDateRange(
                current.start(), current.end());
        long prevLeads = leadRepository.countByDateRange(
                previous.start(), previous.end());

        long currQuotes = quoteRepository.countByDateRange(
                current.start(), current.end());
        long prevQuotes = quoteRepository.countByDateRange(
                previous.start(), previous.end());

        long currShops = repairShopRepository.countApprovedShopsByDateRange(
                current.start(), current.end());
        long prevShops = repairShopRepository.countApprovedShopsByDateRange(
                previous.start(), previous.end());

        double totalGMV = currLeads * GMV_PER_LEAD;

        return AnalyticsStatsDTO.builder()
                .totalLeads(currLeads)
                .totalLeadsChange(calcPercentChange(prevLeads, currLeads))
                .activeShops(currShops)
                .activeShopsChange(calcPercentChange(prevShops, currShops))
                .totalQuotes(currQuotes)
                .totalQuotesChange(calcPercentChange(prevQuotes, currQuotes))
                .totalGMV(totalGMV)
                .gmvChange(GMV_CHANGE_HARDCODED)
                .build();
    }

    private List<TrendPointDTO> buildTrend(
            DateRange range, String period,
            String startDate, String endDate) {

        // Custom range mein agar > 60 days tu monthly, warna daily
        boolean isCustom = startDate != null && endDate != null;
        boolean useMonthly = "12M".equalsIgnoreCase(period) ||
                (isCustom && ChronoUnit.DAYS.between(
                        range.start(), range.end()) > 60);

        List<Object[]> rows = useMonthly
                ? leadRepository.countGroupedByMonth(range.start(), range.end())
                : leadRepository.countGroupedByDay(range.start(), range.end());

        return rows.stream()
                .map(row -> TrendPointDTO.builder()
                        .date((String) row[0])
                        .count(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    private StatusDistributionDTO buildStatusDistribution(DateRange range) {
        long completed = leadRepository.countByStatusAndDateRange(
                "CLOSED", range.start(), range.end());
        long cancelled = leadRepository.countByStatusAndDateRange(
                "CANCELLED", range.start(), range.end());
        long open = leadRepository.countByStatusAndDateRange(
                "OPEN", range.start(), range.end());

        long total = completed + cancelled + open;

        if (total == 0) {
            return StatusDistributionDTO.builder()
                    .completed(0).cancelled(0).other(0).total(0)
                    .build();
        }

        return StatusDistributionDTO.builder()
                .completed(roundTwo((double) completed / total * 100))
                .cancelled(roundTwo((double) cancelled / total * 100))
                .other(roundTwo((double) open / total * 100))
                .total(total)
                .build();
    }

    private List<TopShopDTO> buildTopShops(DateRange range) {
        List<Object[]> rows = repairShopRepository.findTopShopsByAcceptedQuotes(
                range.start(), range.end());

        // If no data in range, fallback to all-time
        if (rows.isEmpty()) {
            rows = repairShopRepository.findTopShopsByAcceptedQuotes(
                    LocalDateTime.of(2000, 1, 1, 0, 0),
                    LocalDateTime.now());
        }

        AtomicInteger rankCounter = new AtomicInteger(1);

        return rows.stream().map(row -> {
            Long shopId    = ((Number) row[0]).longValue();
            String name    = (String) row[1];
            String address = (String) row[2];
            boolean active = (Boolean) row[3];
            long bookings  = ((Number) row[4]).longValue();
            int rank       = rankCounter.getAndIncrement();

            double gmv    = bookings * GMV_PER_LEAD;
            double rating = HARDCODED_RATINGS.getOrDefault(rank, DEFAULT_RATING);

            return TopShopDTO.builder()
                    .shopId(shopId)
                    .shopName(name)
                    .location(address)
                    .bookings(bookings)
                    .gmv(gmv)
                    .rating(rating)
                    .status(active ? "ACTIVE" : "INACTIVE")
                    .build();
        }).collect(Collectors.toList());
    }

    private double calcPercentChange(long previous, long current) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return roundTwo(((double)(current - previous) / previous) * 100);
    }

    private double roundTwo(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private DateRange getDateRange(
            String period, String startDate, String endDate) {

        // Custom range
        if (startDate != null && endDate != null) {
            return new DateRange(
                    LocalDate.parse(startDate).atStartOfDay(),
                    LocalDate.parse(endDate).atTime(23, 59, 59)
            );
        }
        // Period as before
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = switch (period.toUpperCase()) {
            case "7D"  -> now.minusDays(7);
            case "30D" -> now.minusDays(30);
            case "12M" -> now.minusMonths(12);
            default    -> now.minusDays(30);
        };
        return new DateRange(start, now);
    }
    private DateRange getPreviousDateRange(
            String period, String startDate, String endDate) {

        if (startDate != null && endDate != null) {
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            LocalDateTime end   = LocalDate.parse(endDate).atTime(23, 59, 59);
            long days = ChronoUnit.DAYS.between(start, end) + 1;
            return new DateRange(start.minusDays(days), end.minusDays(days));
        }
        // Period as before
        LocalDateTime now = LocalDateTime.now();
        return switch (period.toUpperCase()) {
            case "7D"  -> new DateRange(now.minusDays(14), now.minusDays(7));
            case "30D" -> new DateRange(now.minusDays(60), now.minusDays(30));
            case "12M" -> new DateRange(now.minusMonths(24), now.minusMonths(12));
            default    -> new DateRange(now.minusDays(60), now.minusDays(30));
        };
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {}
}