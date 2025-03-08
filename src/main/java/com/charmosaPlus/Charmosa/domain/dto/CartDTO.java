package com.charmosaPlus.Charmosa.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class CartDTO {
    private Long id;
    private List<CartItemDTO> items;
    private Double totalAmount;
    private Double discountAmount;
}
