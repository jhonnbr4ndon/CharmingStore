package com.charmosaPlus.Charmosa.controller;

import com.charmosaPlus.Charmosa.Service.ProductService;
import com.charmosaPlus.Charmosa.domain.Product;
import com.charmosaPlus.Charmosa.domain.dto.ProductDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return productService.findById(id)
                .map(this::convertToResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{productId}/images/{imageId}")
    public ResponseEntity<byte[]> getProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        return productService.findImageById(productId, imageId)
                .map(image -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, detectImageType(image.getImage()))
                        .body(image.getImage()))
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam(value = "colors", required = false) List<String> colors,
            @RequestParam(value = "sizes", required = false) List<String> sizes,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) { //O Spring Boot pode não interpretar corretamente List<MultipartFile> em @RequestPart, especialmente em requisições multipart.
            //@RequestPart("images") List<MultipartFile> images) {
        try {
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setColors(colors);
            product.setSizes(sizes);

            Product savedProduct = productService.saveWithImages(product, images);
            return ResponseEntity.ok(convertToResponseDTO(savedProduct));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam(value = "colors", required = false) List<String> colors,
            @RequestParam(value = "sizes", required = false) List<String> sizes,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return productService.findById(id)
                .map(product -> {
                    product.setName(name);
                    product.setDescription(description);
                    product.setPrice(price);
                    product.setColors(colors);
                    product.setSizes(sizes);

                    // Atualizar imagens se fornecidas
                    if (images != null && !images.isEmpty()) {
                        productService.updateImages(product, images);
                    }

                    Product updatedProduct = productService.saveWithoutImages(product);
                    return ResponseEntity.ok(convertToResponseDTO(updatedProduct));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable Long id) {
        return productService.findById(id)
                .map(product -> {
                    productService.deleteImagesByProductId(id); // Método apagar imagem, função criada em ProductService.java
                    productService.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private ProductDTO convertToResponseDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setColors(product.getColors());
        dto.setSizes(product.getSizes());

        List<String> imageUrls = product.getImages()
                .stream()
                .map(image -> "/products/" + product.getId() + "/images/" + image.getId())
                .collect(Collectors.toList());
        dto.setImageUrls(imageUrls);

        return dto;
    }

    private String detectImageType(byte[] imageBytes) {
        if (imageBytes.length > 4) {
            String headerHex = String.format("%02x%02x%02x%02x",
                    imageBytes[0], imageBytes[1], imageBytes[2], imageBytes[3]).toUpperCase();

            return switch (headerHex) {
                case "FFD8FFE0", "FFD8FFE1", "FFD8FFE2" -> "image/jpeg";
                case "89504E47" -> "image/png";
                case "47494638" -> "image/gif";
                default -> "application/octet-stream";
            };
        }
        return "application/octet-stream";
    }
}