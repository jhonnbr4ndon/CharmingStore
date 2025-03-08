package com.charmosaPlus.Charmosa.Service;

import com.charmosaPlus.Charmosa.Repository.ProductImageRepository;
import com.charmosaPlus.Charmosa.Repository.ProductRepository;
import com.charmosaPlus.Charmosa.domain.Product;
import com.charmosaPlus.Charmosa.domain.ProductImage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<ProductImage> findAllImagesByProductId(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        return product.getImages();
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

    public Product saveWithoutImages(Product product) {
        return productRepository.save(product);
    }

    // Atualizar uma imagem específica ou todas, dependendo do parâmetro
    public void updateProductImages(Long productId, Long imageId, List<MultipartFile> newImages) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (imageId != null) {
            // Atualiza apenas UMA imagem específica
            ProductImage productImage = product.getImages().stream()
                    .filter(img -> img.getId().equals(imageId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Imagem não encontrada"));

            try {
                productImage.setImage(newImages.get(0).getBytes()); // Apenas a primeira imagem
                productImageRepository.save(productImage);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao processar a imagem", e);
            }
        } else {
            // Substitui TODAS as imagens do produto
            productImageRepository.deleteAll(product.getImages());
            product.getImages().clear();

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
            productRepository.save(product);
        }
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

    public Page<Product> findAllPaginated(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
}