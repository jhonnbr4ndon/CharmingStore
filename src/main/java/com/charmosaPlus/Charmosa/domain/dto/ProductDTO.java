package com.charmosaPlus.Charmosa.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private List<String> colors;
    private List<String> sizes;
    private List<String> imageUrls;
}