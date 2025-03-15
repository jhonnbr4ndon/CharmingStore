package com.charmosaPlus.Charmosa.domain;

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

    private String code; // Código do cupom
    private Double discountPercentage; // Desconto em porcentagem

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true) // Pode ser nulo para cupons gerais
    @JsonIgnoreProperties({"images", "sizes", "colors", "quantity", "description"}) // Ignorar serialização
    private Product product;

    private LocalDate expirationDate; // Data de expiração do cupom
}