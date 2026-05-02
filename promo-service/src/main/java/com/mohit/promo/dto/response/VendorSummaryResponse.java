package com.mohit.promo.dto.response;

import com.mohit.promo.entity.VendorStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorSummaryResponse {
    private Long id;
    private String vendorCode;
    private String vendorName;
    private String email;
    private VendorStatus status;
}