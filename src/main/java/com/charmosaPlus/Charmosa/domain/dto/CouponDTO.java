package com.charmosaPlus.Charmosa.domain.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CouponDTO {
    private Long id;
    private String code;
    private String description;
    private String discountType;
    private Double discountValue;
    private Double minimumAmountToApply;
    private Boolean individualUseOnly;
    private Boolean freeShipping;
    private Integer maxUses;
    private Integer timesUsed;
    private Long productId;
    private LocalDate expirationDate;
}
