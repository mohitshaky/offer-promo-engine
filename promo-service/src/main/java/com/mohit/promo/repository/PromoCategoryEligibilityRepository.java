package com.mohit.promo.repository;

import com.mohit.promo.entity.PromoCategoryEligibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromoCategoryEligibilityRepository extends JpaRepository<PromoCategoryEligibility, Long> {

    List<PromoCategoryEligibility> findByPromoCodeId(Long promoCodeId);

    List<PromoCategoryEligibility> findByCategoryId(Long categoryId);

    boolean existsByPromoCodeIdAndCategoryId(Long promoCodeId, Long categoryId);

    @Modifying
    @Query("DELETE FROM PromoCategoryEligibility pce WHERE pce.promoCode.id = :promoCodeId")
    void deleteByPromoCodeId(@Param("promoCodeId") Long promoCodeId);

    @Modifying
    @Query("DELETE FROM PromoCategoryEligibility pce WHERE pce.promoCode.id = :promoCodeId AND pce.category.id IN :categoryIds")
    void deleteByPromoCodeIdAndCategoryIds(@Param("promoCodeId") Long promoCodeId, @Param("categoryIds") List<Long> categoryIds);
}