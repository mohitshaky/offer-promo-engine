package com.mohit.promo.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PromoValidationRequest {

    @NotBlank(message = "Promo code is required")
    private String promoCode;

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;

    @NotNull(message = "Order amount is required")
    @DecimalMin(value = "0.01", message = "Order amount must be greater than 0")
    private BigDecimal orderAmount;

    private List<Long> categoryIds;

    private String orderId; // For usage tracking
}
