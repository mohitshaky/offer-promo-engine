package com.mohit.promo.controller;

import com.mohit.promo.dto.request.PromoValidationRequest;
import com.mohit.promo.dto.response.ApiResponse;
import com.mohit.promo.dto.response.PromoUsageResponse;
import com.mohit.promo.dto.response.PromoValidationResponse;
import com.mohit.promo.service.PromoAnalyticsService;
import com.mohit.promo.service.PromoValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendor/promo-codes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vendor Promo Operations", description = "APIs for vendors to validate and apply promo codes")
//@PreAuthorize("hasRole('VENDOR')")
public class VendorPromoController {

    private final PromoValidationService promoValidationService;
    private final PromoAnalyticsService promoAnalyticsService;

    @PostMapping("/validate")
    @Operation(summary = "Validate a promo code for an order")
    public ResponseEntity<ApiResponse<PromoValidationResponse>> validatePromoCode(
            @Valid @RequestBody PromoValidationRequest request) {

        log.info("Vendor {} validating promo code: {}", request.getVendorId(), request.getPromoCode());
        PromoValidationResponse response = promoValidationService.validatePromoCode(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/apply")
    @Operation(summary = "Apply a promo code to an order")
    public ResponseEntity<ApiResponse<PromoValidationResponse>> applyPromoCode(
            @Valid @RequestBody PromoValidationRequest request) {

        log.info("Vendor {} applying promo code: {} to order: {}",
                request.getVendorId(), request.getPromoCode(), request.getOrderId());
        PromoValidationResponse response = promoValidationService.applyPromoCode(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/usages")
    @Operation(summary = "Get promo usage history for the vendor")
    public ResponseEntity<ApiResponse<Page<PromoUsageResponse>>> getVendorPromoUsages(
            @RequestHeader("X-Vendor-Id") Long vendorId,
            Pageable pageable) {

        Page<PromoUsageResponse> response = promoAnalyticsService.getVendorPromoUsages(vendorId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}