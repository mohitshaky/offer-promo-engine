package com.mohit.promo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PromoUsageResponse {
    private Long id;
    private String promoCode;
    private String promoTitle;
    private String vendorName;
    private String orderId;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private LocalDateTime usedAt;
}