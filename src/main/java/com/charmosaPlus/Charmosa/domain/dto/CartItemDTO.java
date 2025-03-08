package com.charmosaPlus.Charmosa.domain.dto;

import lombok.Data;

@Data
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String selectedColor;
    private String selectedSize;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
}