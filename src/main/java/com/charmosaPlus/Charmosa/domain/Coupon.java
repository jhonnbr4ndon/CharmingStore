package com.charmosaPlus.Charmosa.domain;

import com.charmosaPlus.Charmosa.domain.enums.DiscountType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String description;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    private Double discountValue;
    private Double minimumAmountToApply;
    private Boolean individualUseOnly;
    private Boolean freeShipping;
    private Integer maxUses;
    private Integer timesUsed = 0;

    private LocalDate expirationDate;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true)
    @JsonIgnoreProperties({"images", "sizes", "colors", "quantity", "description"})
    private Product product;
}