package com.mohit.promo.repository;

import com.mohit.promo.entity.PromoUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromoUsageRepository extends JpaRepository<PromoUsage, Long> {

    Page<PromoUsage> findByPromoCodeId(Long promoCodeId, Pageable pageable);

    Page<PromoUsage> findByVendorId(Long vendorId, Pageable pageable);

    Page<PromoUsage> findByPromoCodeIdAndVendorId(Long promoCodeId, Long vendorId, Pageable pageable);

    @Query("SELECT pu FROM PromoUsage pu WHERE pu.usedAt BETWEEN :startDate AND :endDate")
    List<PromoUsage> findUsagesBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Analytics queries
    @Query("SELECT COUNT(pu) FROM PromoUsage pu WHERE pu.promoCode.id = :promoCodeId")
    Long countTotalUsageByPromoCode(@Param("promoCodeId") Long promoCodeId);

    @Query("SELECT COUNT(DISTINCT pu.vendor.id) FROM PromoUsage pu WHERE pu.promoCode.id = :promoCodeId")
    Long countUniqueVendorsByPromoCode(@Param("promoCodeId") Long promoCodeId);

    @Query("SELECT SUM(pu.discountAmount) FROM PromoUsage pu WHERE pu.promoCode.id = :promoCodeId")
    BigDecimal sumDiscountAmountByPromoCode(@Param("promoCodeId") Long promoCodeId);

    @Query("SELECT SUM(pu.originalAmount) FROM PromoUsage pu WHERE pu.promoCode.id = :promoCodeId")
    BigDecimal sumOriginalAmountByPromoCode(@Param("promoCodeId") Long promoCodeId);

    @Query("SELECT AVG(pu.originalAmount) FROM PromoUsage pu WHERE pu.promoCode.id = :promoCodeId")
    BigDecimal avgOriginalAmountByPromoCode(@Param("promoCodeId") Long promoCodeId);

    @Query("SELECT MAX(pu.usedAt) FROM PromoUsage pu WHERE pu.promoCode.id = :promoCodeId")
    LocalDateTime findLastUsageByPromoCode(@Param("promoCodeId") Long promoCodeId);

    // Vendor-specific analytics
    @Query("SELECT v.vendorName, COUNT(pu), SUM(pu.discountAmount), SUM(pu.originalAmount) " +
            "FROM PromoUsage pu JOIN pu.vendor v WHERE pu.promoCode.id = :promoCodeId " +
            "GROUP BY v.id, v.vendorName ORDER BY COUNT(pu) DESC")
    List<Object[]> findUsageAnalyticsByVendor(@Param("promoCodeId") Long promoCodeId);

    // Time-based analytics
    @Query("SELECT DATE(pu.usedAt), COUNT(pu), SUM(pu.discountAmount) " +
            "FROM PromoUsage pu WHERE pu.promoCode.id = :promoCodeId " +
            "AND pu.usedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(pu.usedAt) ORDER BY DATE(pu.usedAt)")
    List<Object[]> findDailyUsageAnalytics(@Param("promoCodeId") Long promoCodeId,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    // Top performing promos
    @Query("SELECT pc.code, pc.title, COUNT(pu), SUM(pu.discountAmount), SUM(pu.originalAmount) " +
            "FROM PromoUsage pu JOIN pu.promoCode pc " +
            "WHERE pu.usedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY pc.id, pc.code, pc.title " +
            "ORDER BY COUNT(pu) DESC")
    List<Object[]> findTopPerformingPromos(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           Pageable pageable);
}