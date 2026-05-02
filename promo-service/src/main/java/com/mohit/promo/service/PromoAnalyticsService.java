package com.mohit.promo.service;

import com.mohit.promo.dto.response.PromoAnalyticsResponse;
import com.mohit.promo.dto.response.PromoUsageResponse;
import com.mohit.promo.entity.PromoCode;
import com.mohit.promo.entity.PromoUsage;
import com.mohit.promo.exception.PromoCodeNotFoundException;
import com.mohit.promo.repository.PromoCodeRepository;
import com.mohit.promo.repository.PromoUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PromoAnalyticsService {

    private final PromoCodeRepository promoCodeRepository;
    private final PromoUsageRepository promoUsageRepository;

    public PromoAnalyticsResponse getPromoAnalytics(Long promoCodeId) {
        log.info("Generating analytics for promo code ID: {}", promoCodeId);

        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
                .orElseThrow(() -> new PromoCodeNotFoundException("Promo code not found: " + promoCodeId));

        // Basic metrics
        Long totalUsages = promoUsageRepository.countTotalUsageByPromoCode(promoCodeId);
        Long uniqueVendors = promoUsageRepository.countUniqueVendorsByPromoCode(promoCodeId);
        BigDecimal totalDiscountGiven = promoUsageRepository.sumDiscountAmountByPromoCode(promoCodeId);
        BigDecimal totalOrderValue = promoUsageRepository.sumOriginalAmountByPromoCode(promoCodeId);
        BigDecimal averageOrderValue = promoUsageRepository.avgOriginalAmountByPromoCode(promoCodeId);
        LocalDateTime lastUsed = promoUsageRepository.findLastUsageByPromoCode(promoCodeId);

        // Usage by vendor
        Map<String, Object> usageByVendor = getUsageByVendor(promoCodeId);

        // Usage over time (last 30 days)
        Map<String, Object> usageOverTime = getUsageOverTime(promoCodeId, 30);

        return PromoAnalyticsResponse.builder()
                .promoCode(promoCode.getCode())
                .title(promoCode.getTitle())
                .totalUsages(totalUsages != null ? totalUsages.intValue() : 0)
                .uniqueVendors(uniqueVendors != null ? uniqueVendors.intValue() : 0)
                .totalDiscountGiven(totalDiscountGiven != null ? totalDiscountGiven : BigDecimal.ZERO)
                .totalOrderValue(totalOrderValue != null ? totalOrderValue : BigDecimal.ZERO)
                .averageOrderValue(averageOrderValue != null ? averageOrderValue : BigDecimal.ZERO)
                .lastUsed(lastUsed)
                .usageByVendor(usageByVendor)
                .usageOverTime(usageOverTime)
                .build();
    }

    public Page<PromoUsageResponse> getPromoUsages(Long promoCodeId, Pageable pageable) {
        Page<PromoUsage> usages = promoUsageRepository.findByPromoCodeId(promoCodeId, pageable);
        return usages.map(this::mapToUsageResponse);
    }

    public Page<PromoUsageResponse> getVendorPromoUsages(Long vendorId, Pageable pageable) {
        Page<PromoUsage> usages = promoUsageRepository.findByVendorId(vendorId, pageable);
        return usages.map(this::mapToUsageResponse);
    }

    public List<PromoAnalyticsResponse> getTopPerformingPromos(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        log.info("Getting top {} performing promos between {} and {}", limit, startDate, endDate);

        Pageable pageable = Pageable.ofSize(limit);
        List<Object[]> results = promoUsageRepository.findTopPerformingPromos(startDate, endDate, pageable);

        return results.stream()
                .map(row -> PromoAnalyticsResponse.builder()
                        .promoCode((String) row[0])
                        .title((String) row[1])
                        .totalUsages(((Number) row[2]).intValue())
                        .totalDiscountGiven((BigDecimal) row[3])
                        .totalOrderValue((BigDecimal) row[4])
                        .build())
                .collect(Collectors.toList());
    }

    private Map<String, Object> getUsageByVendor(Long promoCodeId) {
        List<Object[]> results = promoUsageRepository.findUsageAnalyticsByVendor(promoCodeId);

        Map<String, Object> usageByVendor = new LinkedHashMap<>();
        for (Object[] row : results) {
            String vendorName = (String) row[0];
            Long usageCount = ((Number) row[1]).longValue();
            BigDecimal discountAmount = (BigDecimal) row[2];
            BigDecimal orderValue = (BigDecimal) row[3];

            Map<String, Object> vendorData = new HashMap<>();
            vendorData.put("usageCount", usageCount);
            vendorData.put("discountAmount", discountAmount);
            vendorData.put("orderValue", orderValue);

            usageByVendor.put(vendorName, vendorData);
        }

        return usageByVendor;
    }

    private Map<String, Object> getUsageOverTime(Long promoCodeId, int days) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        List<Object[]> results = promoUsageRepository.findDailyUsageAnalytics(promoCodeId, startDate, endDate);

        Map<String, Object> usageOverTime = new LinkedHashMap<>();
        for (Object[] row : results) {
            String date = row[0].toString();
            Long usageCount = ((Number) row[1]).longValue();
            BigDecimal discountAmount = (BigDecimal) row[2];

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("usageCount", usageCount);
            dayData.put("discountAmount", discountAmount);

            usageOverTime.put(date, dayData);
        }

        return usageOverTime;
    }

    private PromoUsageResponse mapToUsageResponse(PromoUsage usage) {
        return PromoUsageResponse.builder()
                .id(usage.getId())
                .promoCode(usage.getPromoCode().getCode())
                .promoTitle(usage.getPromoCode().getTitle())
                .vendorName(usage.getVendor().getVendorName())
                .orderId(usage.getOrderId())
                .originalAmount(usage.getOriginalAmount())
                .discountAmount(usage.getDiscountAmount())
                .finalAmount(usage.getFinalAmount())
                .usedAt(usage.getUsedAt())
                .build();
    }
}