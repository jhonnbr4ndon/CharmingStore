package com.charmosaPlus.Charmosa.domain.dto;

import lombok.Data;

@Data
public class CouponDTO {
    private Long id;
    private String code;
    private Double discountPercentage;
    private Long productId;
}
