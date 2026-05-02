package com.mohit.promo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class PromoAnalyticsResponse {
    private String promoCode;
    private String title;
    private Integer totalUsages;
    private Integer uniqueVendors;
    private BigDecimal totalDiscountGiven;
    private BigDecimal totalOrderValue;
    private BigDecimal averageOrderValue;
    private LocalDateTime lastUsed;
    private Map<String, Object> usageByVendor;
    private Map<String, Object> usageOverTime;
}