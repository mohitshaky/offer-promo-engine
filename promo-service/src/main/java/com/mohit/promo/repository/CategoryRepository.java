package com.mohit.promo.repository;

import com.mohit.promo.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Category> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query("SELECT c FROM Category c JOIN c.promoEligibilities pe WHERE pe.promoCode.id = :promoCodeId")
    List<Category> findEligibleCategoriesForPromo(@Param("promoCodeId") Long promoCodeId);
}