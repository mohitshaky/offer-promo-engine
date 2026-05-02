package com.mohit.promo.service;

import com.mohit.promo.dto.request.PromoValidationRequest;
import com.mohit.promo.dto.response.PromoValidationResponse;
import com.mohit.promo.entity.*;
import com.mohit.promo.exception.PromoCodeNotFoundException;
import com.mohit.promo.exception.VendorNotFoundException;
import com.mohit.promo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PromoValidationService {

    private final PromoCodeRepository promoCodeRepository;
    private final VendorRepository vendorRepository;
    private final PromoVendorEligibilityRepository promoVendorEligibilityRepository;
    private final PromoCategoryEligibilityRepository promoCategoryEligibilityRepository;
    private final PromoUsageRepository promoUsageRepository;

    public PromoValidationResponse validatePromoCode(PromoValidationRequest request) {
        log.info("Validating promo code: {} for vendor: {}", request.getPromoCode(), request.getVendorId());

        try {
            // Find promo code
            PromoCode promoCode = promoCodeRepository.findByCode(request.getPromoCode().toUpperCase())
                    .orElseThrow(() -> new PromoCodeNotFoundException("Invalid promo code: " + request.getPromoCode()));

            // Find vendor
            Vendor vendor = vendorRepository.findById(request.getVendorId())
                    .orElseThrow(() -> new VendorNotFoundException("Vendor not found: " + request.getVendorId()));

            // Validate promo code
            String validationMessage = validatePromoCodeInternal(promoCode, vendor, request);

            if (validationMessage != null) {
                return PromoValidationResponse.builder()
                        .isValid(false)
                        .message(validationMessage)
                        .originalAmount(request.getOrderAmount())
                        .discountAmount(BigDecimal.ZERO)
                        .finalAmount(request.getOrderAmount())
                        .promoCode(promoCode.getCode())
                        .build();
            }

            // Calculate discount
            BigDecimal discountAmount = calculateDiscount(promoCode, request.getOrderAmount());
            BigDecimal finalAmount = request.getOrderAmount().subtract(discountAmount);

            return PromoValidationResponse.builder()
                    .isValid(true)
                    .message("Promo code applied successfully")
                    .originalAmount(request.getOrderAmount())
                    .discountAmount(discountAmount)
                    .finalAmount(finalAmount)
                    .promoCode(promoCode.getCode())
                    .promoTitle(promoCode.getTitle())
                    .build();

        } catch (Exception e) {
            log.error("Error validating promo code: {}", e.getMessage());
            return PromoValidationResponse.builder()
                    .isValid(false)
                    .message("Invalid promo code")
                    .originalAmount(request.getOrderAmount())
                    .discountAmount(BigDecimal.ZERO)
                    .finalAmount(request.getOrderAmount())
                    .build();
        }
    }

    public PromoValidationResponse applyPromoCode(PromoValidationRequest request) {
        log.info("Applying promo code: {} for vendor: {} on order: {}",
                request.getPromoCode(), request.getVendorId(), request.getOrderId());

        // First validate the promo code
        PromoValidationResponse validationResponse = validatePromoCode(request);

        if (!validationResponse.getIsValid()) {
            return validationResponse;
        }

        // If valid, record the usage
        PromoCode promoCode = promoCodeRepository.findByCode(request.getPromoCode().toUpperCase()).get();
        Vendor vendor = vendorRepository.findById(request.getVendorId()).get();

        PromoUsage promoUsage = PromoUsage.builder()
                .promoCode(promoCode)
                .vendor(vendor)
                .orderId(request.getOrderId())
                .originalAmount(validationResponse.getOriginalAmount())
                .discountAmount(validationResponse.getDiscountAmount())
                .finalAmount(validationResponse.getFinalAmount())
                .build();

        promoUsageRepository.save(promoUsage);

        log.info("Successfully applied promo code: {} for order: {}", promoCode.getCode(), request.getOrderId());
        return validationResponse;
    }

    private String validatePromoCodeInternal(PromoCode promoCode, Vendor vendor, PromoValidationRequest request) {
        LocalDateTime now = LocalDateTime.now();

        // Check if promo code is active
        if (promoCode.getStatus() != PromoStatus.ACTIVE) {
            return "Promo code is not active";
        }

        // Check if promo code has started
        if (now.isBefore(promoCode.getStartDate())) {
            return "Promo code is not yet active";
        }

        // Check if promo code has expired
        if (now.isAfter(promoCode.getEndDate())) {
            return "Promo code has expired";
        }

        // Check vendor status
        if (vendor.getStatus() != VendorStatus.ACTIVE) {
            return "Vendor account is not active";
        }

        // Check minimum order amount
        if (request.getOrderAmount().compareTo(promoCode.getMinimumOrderAmount()) < 0) {
            return String.format("Minimum order amount is %s", promoCode.getMinimumOrderAmount());
        }

        // Check vendor eligibility
        if (promoCode.getIsVendorSpecific()) {
            boolean isEligible = promoVendorEligibilityRepository.existsByPromoCodeIdAndVendorId(
                    promoCode.getId(), vendor.getId());
            if (!isEligible) {
                return "Promo code is not applicable for this vendor";
            }
        }

        // Check category eligibility
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<PromoCategoryEligibility> categoryEligibilities =
                    promoCategoryEligibilityRepository.findByPromoCodeId(promoCode.getId());

            if (!categoryEligibilities.isEmpty()) {
                boolean hasEligibleCategory = categoryEligibilities.stream()
                        .anyMatch(eligibility -> request.getCategoryIds().contains(eligibility.getCategory().getId()));

                if (!hasEligibleCategory) {
                    return "Promo code is not applicable for selected products";
                }
            }
        }

        // Check usage limits
        Integer totalUsage = promoCodeRepository.countUsageByPromoCodeId(promoCode.getId());
        if (totalUsage >= promoCode.getUsageLimit()) {
            return "Promo code usage limit exceeded";
        }

        // Check vendor-specific usage limit
        Integer vendorUsage = promoCodeRepository.countUsageByPromoCodeIdAndVendorId(
                promoCode.getId(), vendor.getId());
        if (vendorUsage >= promoCode.getUsageLimitPerVendor()) {
            return "Promo code usage limit exceeded for this vendor";
        }

        return null; // No validation errors
    }

    private BigDecimal calculateDiscount(PromoCode promoCode, BigDecimal orderAmount) {
        BigDecimal discountAmount;

        switch (promoCode.getPromoType()) {
            case PERCENTAGE:
                discountAmount = orderAmount.multiply(promoCode.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                break;
            case FIXED_AMOUNT:
                discountAmount = promoCode.getDiscountValue();
                break;
            case FREE_SHIPPING:
                // For free shipping, discount amount would be calculated based on shipping cost
                // For now, using a fixed amount
                discountAmount = BigDecimal.valueOf(50.00); // Assuming default shipping cost
                break;
            default:
                discountAmount = BigDecimal.ZERO;
        }

        // Apply maximum discount limit
        if (promoCode.getMaximumDiscountAmount() != null &&
                discountAmount.compareTo(promoCode.getMaximumDiscountAmount()) > 0) {
            discountAmount = promoCode.getMaximumDiscountAmount();
        }

        // Ensure discount doesn't exceed order amount
        if (discountAmount.compareTo(orderAmount) > 0) {
            discountAmount = orderAmount;
        }

        return discountAmount.setScale(2, RoundingMode.HALF_UP);
    }
}