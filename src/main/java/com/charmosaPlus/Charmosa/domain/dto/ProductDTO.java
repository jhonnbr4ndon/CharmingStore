package com.charmosaPlus.Charmosa.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private List<String> colors;
    private List<String> sizes;
    private List<String> imageUrls;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public List<String> getSizes() {
        return sizes;
    }

    public void setSizes(List<String> sizes) {
        this.sizes = sizes;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}