package com.mohit.promo.service;

import com.mohit.promo.dto.request.PromoCodeCreateRequest;
import com.mohit.promo.dto.request.PromoCodeUpdateRequest;
import com.mohit.promo.dto.response.PromoCodeResponse;
import com.mohit.promo.entity.*;
import com.mohit.promo.exception.PromoCodeAlreadyExistsException;
import com.mohit.promo.exception.PromoCodeNotFoundException;
import com.mohit.promo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.mohit.promo.service.PromoCodeSpecification.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;
    private final VendorRepository vendorRepository;
    private final CategoryRepository categoryRepository;
    private final PromoVendorEligibilityRepository promoVendorEligibilityRepository;
    private final PromoCategoryEligibilityRepository promoCategoryEligibilityRepository;
    private final PromoUsageRepository promoUsageRepository;

    public PromoCodeResponse createPromoCode(PromoCodeCreateRequest request, Long createdBy) {
        log.info("Creating promo code: {}", request.getCode());

        // Check if promo code already exists
        if (promoCodeRepository.findByCode(request.getCode()).isPresent()) {
            throw new PromoCodeAlreadyExistsException("Promo code already exists: " + request.getCode());
        }

        // Create promo code entity
        PromoCode promoCode = PromoCode.builder()
                .code(request.getCode().toUpperCase())
                .title(request.getTitle())
                .description(request.getDescription())
                .promoType(request.getPromoType())
                .discountValue(request.getDiscountValue())
                .minimumOrderAmount(request.getMinimumOrderAmount())
                .maximumDiscountAmount(request.getMaximumDiscountAmount())
                .usageLimit(request.getUsageLimit())
                .usageLimitPerVendor(request.getUsageLimitPerVendor())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isVendorSpecific(request.getIsVendorSpecific())
                .status(PromoStatus.ACTIVE)
                .createdBy(createdBy)
                .build();

        promoCode = promoCodeRepository.save(promoCode);

        // Handle vendor eligibility
        if (request.getIsVendorSpecific() && request.getEligibleVendorIds() != null) {
            createVendorEligibilities(promoCode, request.getEligibleVendorIds());
        }

        // Handle category eligibility
        if (request.getEligibleCategoryIds() != null && !request.getEligibleCategoryIds().isEmpty()) {
            createCategoryEligibilities(promoCode, request.getEligibleCategoryIds());
        }

        log.info("Successfully created promo code: {}", promoCode.getCode());
        return mapToResponse(promoCode);
    }

    public PromoCodeResponse updatePromoCode(Long promoCodeId, PromoCodeUpdateRequest request) {
        log.info("Updating promo code ID: {}", promoCodeId);

        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
                .orElseThrow(() -> new PromoCodeNotFoundException("Promo code not found: " + promoCodeId));

        // Update fields if provided
        if (request.getTitle() != null) {
            promoCode.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            promoCode.setDescription(request.getDescription());
        }
        if (request.getDiscountValue() != null) {
            promoCode.setDiscountValue(request.getDiscountValue());
        }
        if (request.getMinimumOrderAmount() != null) {
            promoCode.setMinimumOrderAmount(request.getMinimumOrderAmount());
        }
        if (request.getMaximumDiscountAmount() != null) {
            promoCode.setMaximumDiscountAmount(request.getMaximumDiscountAmount());
        }
        if (request.getUsageLimit() != null) {
            promoCode.setUsageLimit(request.getUsageLimit());
        }
        if (request.getUsageLimitPerVendor() != null) {
            promoCode.setUsageLimitPerVendor(request.getUsageLimitPerVendor());
        }
        if (request.getEndDate() != null) {
            promoCode.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            promoCode.setStatus(request.getStatus());
        }

        promoCode = promoCodeRepository.save(promoCode);

        // Update vendor eligibilities if provided
        if (request.getEligibleVendorIds() != null) {
            updateVendorEligibilities(promoCode, request.getEligibleVendorIds());
        }

        // Update category eligibilities if provided
        if (request.getEligibleCategoryIds() != null) {
            updateCategoryEligibilities(promoCode, request.getEligibleCategoryIds());
        }

        log.info("Successfully updated promo code: {}", promoCode.getCode());
        return mapToResponse(promoCode);
    }

    public PromoCodeResponse getPromoCode(Long promoCodeId) {
        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
                .orElseThrow(() -> new PromoCodeNotFoundException("Promo code not found: " + promoCodeId));
        return mapToResponse(promoCode);
    }

    public PromoCodeResponse getPromoCodeByCode(String code) {
        PromoCode promoCode = promoCodeRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new PromoCodeNotFoundException("Promo code not found: " + code));
        return mapToResponse(promoCode);
    }

    public Page<PromoCodeResponse> getAllPromoCodes(Pageable pageable) {
        Page<PromoCode> promoCodes = promoCodeRepository.findAll(pageable);
        return promoCodes.map(this::mapToResponse);
    }

    public Page<PromoCodeResponse> searchPromoCodes(
            String code, String title, PromoStatus status, PromoType promoType, Pageable pageable) {

        Specification<PromoCode> spec = Specification.where(hasCode(code))
                .and(hasTitle(title))
                .and(hasStatus(status))
                .and(hasPromoType(promoType));

        Page<PromoCode> promoCodes = promoCodeRepository.findAll(spec, pageable);

        return promoCodes.map(this::mapToResponse);
    }


    public void deletePromoCode(Long promoCodeId) {
        log.info("Deleting promo code ID: {}", promoCodeId);

        PromoCode promoCode = promoCodeRepository.findById(promoCodeId)
                .orElseThrow(() -> new PromoCodeNotFoundException("Promo code not found: " + promoCodeId));

        // Check if promo code has been used
        Integer usageCount = promoCodeRepository.countUsageByPromoCodeId(promoCodeId);
        if (usageCount > 0) {
            // Don't delete, just deactivate
            promoCode.setStatus(PromoStatus.INACTIVE);
            promoCodeRepository.save(promoCode);
            log.info("Promo code {} deactivated due to existing usage", promoCode.getCode());
        } else {
            // Safe to delete
            promoCodeRepository.delete(promoCode);
            log.info("Promo code {} deleted successfully", promoCode.getCode());
        }
    }

    // Scheduled task to mark expired promo codes
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void markExpiredPromoCodes() {
        log.info("Checking for expired promo codes");
        int expiredCount = promoCodeRepository.markExpiredPromoCodes(LocalDateTime.now());
        if (expiredCount > 0) {
            log.info("Marked {} promo codes as expired", expiredCount);
        }
    }

    private void createVendorEligibilities(PromoCode promoCode, List<Long> vendorIds) {
        List<PromoVendorEligibility> eligibilities = vendorIds.stream()
                .filter(vendorId -> vendorRepository.existsById(vendorId))
                .map(vendorId -> PromoVendorEligibility.builder()
                        .promoCode(promoCode)
                        .vendor(vendorRepository.getReferenceById(vendorId))
                        .build())
                .collect(Collectors.toList());

        promoVendorEligibilityRepository.saveAll(eligibilities);
        log.info("Created {} vendor eligibilities for promo {}", eligibilities.size(), promoCode.getCode());
    }

    private void createCategoryEligibilities(PromoCode promoCode, List<Long> categoryIds) {
        List<PromoCategoryEligibility> eligibilities = categoryIds.stream()
                .filter(categoryId -> categoryRepository.existsById(categoryId))
                .map(categoryId -> PromoCategoryEligibility.builder()
                        .promoCode(promoCode)
                        .category(categoryRepository.getReferenceById(categoryId))
                        .build())
                .collect(Collectors.toList());

        promoCategoryEligibilityRepository.saveAll(eligibilities);
        log.info("Created {} category eligibilities for promo {}", eligibilities.size(), promoCode.getCode());
    }

    private void updateVendorEligibilities(PromoCode promoCode, List<Long> vendorIds) {
        // Remove existing eligibilities
        promoVendorEligibilityRepository.deleteByPromoCodeId(promoCode.getId());

        // Create new eligibilities
        if (!vendorIds.isEmpty()) {
            createVendorEligibilities(promoCode, vendorIds);
        }
    }

    private void updateCategoryEligibilities(PromoCode promoCode, List<Long> categoryIds) {
        // Remove existing eligibilities
        promoCategoryEligibilityRepository.deleteByPromoCodeId(promoCode.getId());

        // Create new eligibilities
        if (!categoryIds.isEmpty()) {
            createCategoryEligibilities(promoCode, categoryIds);
        }
    }

    private PromoCodeResponse mapToResponse(PromoCode promoCode) {
        Integer totalUsage = promoCodeRepository.countUsageByPromoCodeId(promoCode.getId());

        return PromoCodeResponse.builder()
                .id(promoCode.getId())
                .code(promoCode.getCode())
                .title(promoCode.getTitle())
                .description(promoCode.getDescription())
                .promoType(promoCode.getPromoType())
                .discountValue(promoCode.getDiscountValue())
                .minimumOrderAmount(promoCode.getMinimumOrderAmount())
                .maximumDiscountAmount(promoCode.getMaximumDiscountAmount())
                .usageLimit(promoCode.getUsageLimit())
                .usageLimitPerVendor(promoCode.getUsageLimitPerVendor())
                .startDate(promoCode.getStartDate())
                .endDate(promoCode.getEndDate())
                .isVendorSpecific(promoCode.getIsVendorSpecific())
                .status(promoCode.getStatus())
                .createdBy(promoCode.getCreatedBy())
                .createdAt(promoCode.getCreatedAt())
                .updatedAt(promoCode.getUpdatedAt())
                .totalUsageCount(totalUsage)
                .build();
    }
}
