package com.charmosaPlus.Charmosa.Repository;

import com.charmosaPlus.Charmosa.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
