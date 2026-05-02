package com.mohit.promo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "promo_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "promo_type", nullable = false)
    private PromoType promoType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "minimum_order_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal minimumOrderAmount = BigDecimal.ZERO;

    @Column(name = "maximum_discount_amount", precision = 10, scale = 2)
    private BigDecimal maximumDiscountAmount;

    @Column(name = "usage_limit")
    @Builder.Default
    private Integer usageLimit = 1;

    @Column(name = "usage_limit_per_vendor")
    @Builder.Default
    private Integer usageLimitPerVendor = 1;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_vendor_specific")
    @Builder.Default
    private Boolean isVendorSpecific = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PromoStatus status = PromoStatus.ACTIVE;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "promoCode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PromoVendorEligibility> vendorEligibilities;

    @OneToMany(mappedBy = "promoCode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PromoCategoryEligibility> categoryEligibilities;

    @OneToMany(mappedBy = "promoCode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PromoUsage> promoUsages;
}