package com.charmosaPlus.Charmosa.Service;

import com.charmosaPlus.Charmosa.Repository.ProductImageRepository;
import com.charmosaPlus.Charmosa.Repository.ProductRepository;
import com.charmosaPlus.Charmosa.domain.Product;
import com.charmosaPlus.Charmosa.domain.ProductImage;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@CrossOrigin(origins = "http://localhost:4200") // Permite requisições do Angular
@RestController
@RequestMapping("/products")
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public ProductService(ProductRepository productRepository, ProductImageRepository productImageRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<ProductImage> findImageById(Long productId, Long imageId) {
        return productRepository.findById(productId)
                .flatMap(product -> product.getImages()
                        .stream()
                        .filter(image -> image.getId().equals(imageId))
                        .findFirst());
    }

    public Product saveWithImages(Product product, List<MultipartFile> images) {
        List<ProductImage> productImages = images.stream().map(image -> {
            try {
                ProductImage productImage = new ProductImage();
                productImage.setImage(image.getBytes());
                productImage.setProduct(product);
                return productImage;
            } catch (IOException e) {
                throw new RuntimeException("Erro ao processar imagem", e);
            }
        }).collect(Collectors.toList());

        product.setImages(productImages);
        return productRepository.save(product);
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    public Product saveWithoutImages(Product product) {
        if (!isAdmin()) {
            throw new RuntimeException("Acesso negado: Apenas administradores podem adicionar produtos.");
        }
        return productRepository.save(product);
    }

    public void updateImages(Product product, List<MultipartFile> newImages) {
        // Remove imagens antigas
        product.getImages().clear();
        // Adiciona novas imagens
        List<ProductImage> updatedImages = newImages.stream().map(image -> {
            try {
                ProductImage productImage = new ProductImage();
                productImage.setImage(image.getBytes());
                productImage.setProduct(product);
                return productImage;
            } catch (IOException e) {
                throw new RuntimeException("Erro ao processar imagem", e);
            }
        }).collect(Collectors.toList());
        product.getImages().addAll(updatedImages);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    public void deleteImagesByProductId(Long id) { // Método apagar imagem ao apagar produto em ProductController.java
    }
}