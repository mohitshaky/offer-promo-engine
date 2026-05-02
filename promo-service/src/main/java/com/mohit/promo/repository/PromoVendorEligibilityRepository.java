package com.mohit.promo.repository;

import com.mohit.promo.entity.PromoVendorEligibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoVendorEligibilityRepository extends JpaRepository<PromoVendorEligibility, Long> {

    List<PromoVendorEligibility> findByPromoCodeId(Long promoCodeId);

    List<PromoVendorEligibility> findByVendorId(Long vendorId);

    boolean existsByPromoCodeIdAndVendorId(Long promoCodeId, Long vendorId);

    @Modifying
    @Query("DELETE FROM PromoVendorEligibility pve WHERE pve.promoCode.id = :promoCodeId")
    void deleteByPromoCodeId(@Param("promoCodeId") Long promoCodeId);

    @Modifying
    @Query("DELETE FROM PromoVendorEligibility pve WHERE pve.promoCode.id = :promoCodeId AND pve.vendor.id IN :vendorIds")
    void deleteByPromoCodeIdAndVendorIds(@Param("promoCodeId") Long promoCodeId, @Param("vendorIds") List<Long> vendorIds);
}