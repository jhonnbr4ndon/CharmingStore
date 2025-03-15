package com.charmosaPlus.Charmosa.Repository;

import com.charmosaPlus.Charmosa.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);  // Busca um cupom pelo código

    boolean existsByCode(String code); // Retorna true se já existir um cupom com esse código
}