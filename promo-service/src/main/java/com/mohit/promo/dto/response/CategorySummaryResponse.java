package com.mohit.promo.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategorySummaryResponse {
    private Long id;
    private String name;
    private String description;
}