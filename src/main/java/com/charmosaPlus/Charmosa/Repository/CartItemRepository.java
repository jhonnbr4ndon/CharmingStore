package com.charmosaPlus.Charmosa.Repository;

import com.charmosaPlus.Charmosa.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}