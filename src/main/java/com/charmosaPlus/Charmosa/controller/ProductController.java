package com.charmosaPlus.Charmosa.controller;

import com.charmosaPlus.Charmosa.Service.ProductService;
import com.charmosaPlus.Charmosa.domain.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Listar todos os produtos
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.findAll());
    }

    // Buscar produto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Criar um novo produto (Apenas Admin)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.save(product));
    }

    // Atualizar um produto (Apenas Admin)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product productDetails) {
        return productService.findById(id)
                .map(product -> {
                    product.setName(productDetails.getName());
                    product.setDescription(productDetails.getDescription());
                    product.setPrice(productDetails.getPrice());
                    product.setColor(productDetails.getColor());
                    product.setSize(productDetails.getSize());
                    return ResponseEntity.ok(productService.save(product));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Deletar um produto (Apenas Admin)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productService.findById(id).isPresent()) {
            productService.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
