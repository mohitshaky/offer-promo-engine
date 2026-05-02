package com.mohit.promo.service;

import com.mohit.promo.entity.PromoCode;
import com.mohit.promo.entity.PromoStatus;
import com.mohit.promo.entity.PromoType;
import org.springframework.data.jpa.domain.Specification;

public class PromoCodeSpecification {

    public static Specification<PromoCode> hasCode(String code) {
        return (root, query, cb) -> {
            if (code == null || code.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%");
        };
    }

    public static Specification<PromoCode> hasTitle(String title) {
        return (root, query, cb) -> {
            if (title == null || title.isBlank()) return cb.conjunction();
            return cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }

    public static Specification<PromoCode> hasStatus(PromoStatus status) {
        return (root, query, cb) -> {
            if (status == null) return cb.conjunction();
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<PromoCode> hasPromoType(PromoType promoType) {
        return (root, query, cb) -> {
            if (promoType == null) return cb.conjunction();
            return cb.equal(root.get("promoType"), promoType);
        };
    }
}
