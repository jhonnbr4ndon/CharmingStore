package com.charmosaPlus.Charmosa.controller;

import com.charmosaPlus.Charmosa.Service.ProductService;
import com.charmosaPlus.Charmosa.domain.Product;
import com.charmosaPlus.Charmosa.domain.ProductImage;
import com.charmosaPlus.Charmosa.domain.dto.ProductDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    @GetMapping("/{productId}/images")
    public ResponseEntity<?> getProductImages(
            @PathVariable Long productId,
            @RequestParam(required = false) Long imageId) {

        if (imageId != null) {
            // Se imageId foi informado, retorna apenas a imagem específica
            return productService.findImageById(productId, imageId)
                    .map(image -> ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, detectImageType(image.getImage()))
                            .body(image.getImage()))
                    .orElse(ResponseEntity.notFound().build());
        } else {
            // Se imageId não foi informado, retorna todas as imagens do produto
            List<ProductImage> images = productService.findAllImagesByProductId(productId);

            if (images.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // Criando URLs para acessar cada imagem
            List<byte[]> imageBytesList = images.stream()
                    .map(ProductImage::getImage)
                    .collect(Collectors.toList());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(imageBytesList);
        }
    }

    @GetMapping("/{productId}/images/{imageId}")
    public ResponseEntity<byte[]> getProductImageById(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        return productService.findImageById(productId, imageId)
                .map(image -> {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.IMAGE_JPEG);
                    return new ResponseEntity<>(image.getImage(), headers, HttpStatus.OK);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("quantity") Integer quantity,
            @RequestParam(value = "colors", required = false) List<String> colors,
            @RequestParam(value = "sizes", required = false) List<String> sizes,
            @RequestPart("images") List<MultipartFile> images) {
        try {
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setQuantity(quantity);
            product.setColors(cleanList(colors));
            product.setSizes(cleanList(sizes));

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
            @RequestParam("quantity") Integer quantity,
            @RequestParam(value = "colors", required = false) List<String> colors,
            @RequestParam(value = "sizes", required = false) List<String> sizes,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        return productService.findById(id)
                .map(product -> {
                    product.setName(name);
                    product.setDescription(description);
                    product.setPrice(price);
                    product.setQuantity(quantity);
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
    @PutMapping("/{productId}/images")
    public ResponseEntity<String> updateProductImages(
            @PathVariable Long productId,
            @RequestPart("images") List<MultipartFile> images) {

        try {
            productService.updateProductImages(productId, null, images);
            return ResponseEntity.ok("Todas as imagens foram atualizadas!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{productId}/images/{imageId}")
    public ResponseEntity<String> updateSingleProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @RequestPart("images") MultipartFile image) {

        try {
            productService.updateProductImages(productId, imageId, List.of(image));
            return ResponseEntity.ok("Imagem atualizada com sucesso!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

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

    private ProductDTO convertToResponseDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
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

    private List<String> cleanList(List<String> input) {
        if (input == null) return null;

        return input.stream()
                .map(String::trim)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toList());
    }
}