package com.charmosaPlus.Charmosa.domain.dto;

import lombok.Data;

@Data
public class UpdateQuantityDTO {
    private Long itemId;
    private Integer quantity;
}