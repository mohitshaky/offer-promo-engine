package com.mohit.promo.repository;

import com.mohit.promo.entity.Vendor;
import com.mohit.promo.entity.VendorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByVendorCodeAndStatus(String vendorCode, VendorStatus status);

    Optional<Vendor> findByEmailAndStatus(String email, VendorStatus status);

    Page<Vendor> findByStatus(VendorStatus status, Pageable pageable);

    @Query("SELECT v FROM Vendor v WHERE " +
            "(:vendorName IS NULL OR LOWER(v.vendorName) LIKE LOWER(CONCAT('%', :vendorName, '%'))) AND " +
            "(:email IS NULL OR LOWER(v.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:status IS NULL OR v.status = :status)")
    Page<Vendor> findVendorsWithFilters(
            @Param("vendorName") String vendorName,
            @Param("email") String email,
            @Param("status") VendorStatus status,
            Pageable pageable);

    @Query("SELECT v FROM Vendor v JOIN v.promoEligibilities pe WHERE pe.promoCode.id = :promoCodeId")
    List<Vendor> findEligibleVendorsForPromo(@Param("promoCodeId") Long promoCodeId);
}