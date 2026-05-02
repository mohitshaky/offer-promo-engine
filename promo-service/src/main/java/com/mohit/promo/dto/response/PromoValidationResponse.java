package com.mohit.promo.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PromoValidationResponse {
    private Boolean isValid;
    private String message;
    private BigDecimal originalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String promoCode;
    private String promoTitle;
}