package com.mohit.promo.dto.response;

import com.mohit.promo.entity.PromoStatus;
import com.mohit.promo.entity.PromoType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PromoCodeResponse {
    private Long id;
    private String code;
    private String title;
    private String description;
    private PromoType promoType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrderAmount;
    private BigDecimal maximumDiscountAmount;
    private Integer usageLimit;
    private Integer usageLimitPerVendor;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isVendorSpecific;
    private PromoStatus status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer totalUsageCount;
    private List<VendorSummaryResponse> eligibleVendors;
    private List<CategorySummaryResponse> eligibleCategories;
}