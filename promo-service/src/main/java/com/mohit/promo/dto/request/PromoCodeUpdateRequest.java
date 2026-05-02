package com.mohit.promo.dto.request;

import com.mohit.promo.entity.PromoStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PromoCodeUpdateRequest {

    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Discount value is too large")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.00", message = "Minimum order amount cannot be negative")
    private BigDecimal minimumOrderAmount;

    @DecimalMin(value = "0.01", message = "Maximum discount amount must be greater than 0")
    private BigDecimal maximumDiscountAmount;

    @Min(value = 1, message = "Usage limit must be at least 1")
    @Max(value = 1000000, message = "Usage limit is too large")
    private Integer usageLimit;

    @Min(value = 1, message = "Usage limit per vendor must be at least 1")
    @Max(value = 10000, message = "Usage limit per vendor is too large")
    private Integer usageLimitPerVendor;

    private LocalDateTime endDate;

    private PromoStatus status;

    private List<Long> eligibleVendorIds;

    private List<Long> eligibleCategoryIds;
}
