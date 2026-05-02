package com.mohit.promo.repository;

import com.mohit.promo.entity.PromoCode;
import com.mohit.promo.entity.PromoStatus;
import com.mohit.promo.entity.PromoType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long>,
        JpaSpecificationExecutor<PromoCode> {   // 👈 add this

    Optional<PromoCode> findByCodeAndStatus(String code, PromoStatus status);

    Optional<PromoCode> findByCode(String code);

    Page<PromoCode> findByStatus(PromoStatus status, Pageable pageable);

    Page<PromoCode> findByCreatedBy(Long createdBy, Pageable pageable);

    @Query("SELECT p FROM PromoCode p WHERE p.status = :status AND p.startDate <= :now AND p.endDate >= :now")
    List<PromoCode> findActivePromoCodesAt(@Param("status") PromoStatus status, @Param("now") LocalDateTime now);

    @Query("SELECT p FROM PromoCode p WHERE p.endDate < :now AND p.status = 'ACTIVE'")
    List<PromoCode> findExpiredPromoCodes(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE PromoCode p SET p.status = 'EXPIRED' WHERE p.endDate < :now AND p.status = 'ACTIVE'")
    int markExpiredPromoCodes(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(pu) FROM PromoUsage pu WHERE pu.promoCode.id = :promoCodeId")
    Integer countUsageByPromoCodeId(@Param("promoCodeId") Long promoCodeId);

    @Query("SELECT COUNT(pu) FROM PromoUsage pu WHERE pu.promoCode.id = :promoCodeId AND pu.vendor.id = :vendorId")
    Integer countUsageByPromoCodeIdAndVendorId(@Param("promoCodeId") Long promoCodeId, @Param("vendorId") Long vendorId);
}
