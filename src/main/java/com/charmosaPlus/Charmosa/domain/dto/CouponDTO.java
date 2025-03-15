package com.charmosaPlus.Charmosa.domain.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CouponDTO {
    private Long id;
    private String code;
    private Double discountPercentage;
    private Long productId;
    private LocalDate expirationDate; // Data de expiração
}
