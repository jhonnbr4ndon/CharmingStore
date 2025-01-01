package com.charmosaPlus.Charmosa.controller;

import com.charmosaPlus.Charmosa.Service.ProductService;
import com.charmosaPlus.Charmosa.domain.Product;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/products")
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

    // Buscar produto por ID e retornar imagem
    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
        return productService.findById(id)
                .map(product -> {
                    String contentType = detectImageType(product.getImage());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, contentType)
                            .body(product.getImage());
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Criar um novo produto (Apenas Admin)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<Product> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam(value = "colors", required = false, defaultValue = "") String colors, // Default vazio
            @RequestParam(value = "sizes", required = false, defaultValue = "") String sizes,   // Default vazio
            @RequestParam("image") MultipartFile image) {
        try {
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);

            // Converte os valores separados por vírgula para listas (se não estiver vazio)
            if (!colors.isEmpty()) {
                product.setColors(List.of(colors.split(",")));
            }
            if (!sizes.isEmpty()) {
                product.setSizes(List.of(sizes.split(",")));
            }

            product.setImage(image.getBytes()); // Converte o arquivo para byte[]

            return ResponseEntity.ok(productService.save(product));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
                    product.setColors(productDetails.getColors());
                    product.setSizes(productDetails.getSizes());
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

    // Detectar o tipo de imagem dinamicamente
    private String detectImageType(byte[] imageBytes) {
        if (imageBytes.length > 4) {
            String headerHex = String.format("%02x%02x%02x%02x",
                    imageBytes[0], imageBytes[1], imageBytes[2], imageBytes[3]).toUpperCase();

            // Associações de header com tipos de imagem comuns
            switch (headerHex) {
                case "FFD8FFE0": case "FFD8FFE1": case "FFD8FFE2": return "image/jpeg";
                case "89504E47": return "image/png";
                case "47494638": return "image/gif";
                default: return "application/octet-stream";
            }
        }
        return "application/octet-stream";
    }
}