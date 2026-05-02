package com.mohit.promo.controller;

import com.mohit.promo.dto.request.PromoCodeCreateRequest;
import com.mohit.promo.dto.request.PromoCodeUpdateRequest;
import com.mohit.promo.dto.response.ApiResponse;
import com.mohit.promo.dto.response.PromoAnalyticsResponse;
import com.mohit.promo.dto.response.PromoCodeResponse;
import com.mohit.promo.dto.response.PromoUsageResponse;
import com.mohit.promo.entity.PromoStatus;
import com.mohit.promo.entity.PromoType;
import com.mohit.promo.service.PromoAnalyticsService;
import com.mohit.promo.service.PromoCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/promo-codes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin Promo Management", description = "APIs for admin to manage promo codes")
//@PreAuthorize("hasRole('ADMIN')")
public class AdminPromoController {

    private final PromoCodeService promoCodeService;
    private final PromoAnalyticsService promoAnalyticsService;

    @PostMapping
    @Operation(summary = "Create a new promo code")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> createPromoCode(
            @Valid @RequestBody PromoCodeCreateRequest request,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Admin {} creating promo code: {}", userId, request.getCode());
        PromoCodeResponse response = promoCodeService.createPromoCode(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Promo code created successfully", response));
    }

    @PutMapping("/{promoCodeId}")
    @Operation(summary = "Update an existing promo code")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> updatePromoCode(
            @PathVariable Long promoCodeId,
            @Valid @RequestBody PromoCodeUpdateRequest request) {

        log.info("Updating promo code ID: {}", promoCodeId);
        PromoCodeResponse response = promoCodeService.updatePromoCode(promoCodeId, request);
        return ResponseEntity.ok(ApiResponse.success("Promo code updated successfully", response));
    }

    @GetMapping("/{promoCodeId}")
    @Operation(summary = "Get promo code by ID")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> getPromoCode(@PathVariable Long promoCodeId) {
        PromoCodeResponse response = promoCodeService.getPromoCode(promoCodeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get promo code by code")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> getPromoCodeByCode(@PathVariable String code) {
        PromoCodeResponse response = promoCodeService.getPromoCodeByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Get all promo codes with pagination and filters")
    public ResponseEntity<ApiResponse<Page<PromoCodeResponse>>> getAllPromoCodes(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) PromoStatus status,
            @RequestParam(required = false) PromoType promoType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        // Split sort param
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];   // always exists
        Sort.Direction direction = Sort.Direction.DESC; // default

        if (sortParams.length > 1) {
            direction = Sort.Direction.fromString(sortParams[1]);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));


        // Service call
        Page<PromoCodeResponse> response;
        if (code != null || title != null || status != null || promoType != null) {
            response = promoCodeService.searchPromoCodes(code, title, status, promoType, pageable);
        } else {
            response = promoCodeService.getAllPromoCodes(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }






    @DeleteMapping("/{promoCodeId}")
    @Operation(summary = "Delete a promo code")
    public ResponseEntity<ApiResponse<Void>> deletePromoCode(@PathVariable Long promoCodeId) {
        log.info("Deleting promo code ID: {}", promoCodeId);
        promoCodeService.deletePromoCode(promoCodeId);
        return ResponseEntity.ok(ApiResponse.success("Promo code deleted successfully", null));
    }

    @GetMapping("/{promoCodeId}/analytics")
    @Operation(summary = "Get analytics for a specific promo code")
    public ResponseEntity<ApiResponse<PromoAnalyticsResponse>> getPromoAnalytics(@PathVariable Long promoCodeId) {
        PromoAnalyticsResponse response = promoAnalyticsService.getPromoAnalytics(promoCodeId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{promoCodeId}/usages")
    @Operation(summary = "Get usage history for a specific promo code")
    public ResponseEntity<ApiResponse<Page<PromoUsageResponse>>> getPromoUsages(
            @PathVariable Long promoCodeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "usedAt"));
        Page<PromoUsageResponse> usages = promoAnalyticsService.getPromoUsages(promoCodeId, pageable);

        return ResponseEntity.ok(ApiResponse.success(usages));
    }


    @GetMapping("/analytics/top-performing")
    @Operation(summary = "Get top performing promo codes")
    public ResponseEntity<ApiResponse<List<PromoAnalyticsResponse>>> getTopPerformingPromos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "10") int limit) {

        List<PromoAnalyticsResponse> response = promoAnalyticsService.getTopPerformingPromos(startDate, endDate, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
