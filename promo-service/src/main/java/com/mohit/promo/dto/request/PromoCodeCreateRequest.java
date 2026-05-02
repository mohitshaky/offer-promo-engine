package com.mohit.promo.dto.request;

import com.mohit.promo.entity.PromoType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PromoCodeCreateRequest {

    @NotBlank(message = "Promo code is required")
    @Size(min = 3, max = 50, message = "Promo code must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Promo code can only contain uppercase letters, numbers, underscore and hyphen")
    private String code;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Promo type is required")
    private PromoType promoType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Discount value is too large")
    private BigDecimal discountValue;

    @DecimalMin(value = "0.00", message = "Minimum order amount cannot be negative")
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.01", message = "Maximum discount amount must be greater than 0")
    private BigDecimal maximumDiscountAmount;

    @Min(value = 1, message = "Usage limit must be at least 1")
    @Max(value = 1000000, message = "Usage limit is too large")
    private Integer usageLimit = 1;

    @Min(value = 1, message = "Usage limit per vendor must be at least 1")
    @Max(value = 10000, message = "Usage limit per vendor is too large")
    private Integer usageLimitPerVendor = 1;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    private Boolean isVendorSpecific = false;

    private List<Long> eligibleVendorIds;

    private List<Long> eligibleCategoryIds;

    @AssertTrue(message = "End date must be after start date")
    public boolean isEndDateAfterStartDate() {
        return endDate == null || startDate == null || endDate.isAfter(startDate);
    }

    @AssertTrue(message = "Maximum discount amount must be greater than or equal to discount value for fixed amount promos")
    public boolean isMaxDiscountValid() {
        if (promoType == PromoType.FIXED_AMOUNT && maximumDiscountAmount != null) {
            return maximumDiscountAmount.compareTo(discountValue) >= 0;
        }
        return true;
    }
}