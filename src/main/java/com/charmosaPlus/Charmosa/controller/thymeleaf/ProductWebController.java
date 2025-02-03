package com.charmosaPlus.Charmosa.controller.thymeleaf;

import com.charmosaPlus.Charmosa.Service.ProductService;
import com.charmosaPlus.Charmosa.domain.Product;
import com.charmosaPlus.Charmosa.domain.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/web/products")
public class ProductWebController {

    private final ProductService productService;

    public ProductWebController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {

        // Obter página de produtos
        Page<Product> productPage = productService.findAllPaginated(PageRequest.of(page, size));

        // Converter produtos para DTOs
        List<ProductDTO> productDTOs = productPage.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Adicionar atributos ao modelo
        model.addAttribute("productPage", productDTOs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("content", "product-list"); // Nome do fragmento a ser incluído

        return "home"; // Renderiza o layout principal
    }

    @GetMapping("/create")
    public String createProduct(Model model) {
        model.addAttribute("content", "create-product");
        return "home"; // Renderiza o layout principal
    }

    @PostMapping("/create")
    public String saveProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam(value = "colors", required = false) String colors,
            @RequestParam(value = "sizes", required = false) String sizes,
            @RequestParam("images") List<MultipartFile> images,
            Model model) {

        try {
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);

            // Separar cores e tamanhos em listas
            product.setColors(Arrays.asList(colors.split(",")));
            product.setSizes(Arrays.asList(sizes.split(",")));

            productService.saveWithImages(product, images);

            model.addAttribute("successMessage", "Produto cadastrado com sucesso!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erro ao salvar produto: " + e.getMessage());
        }

        model.addAttribute("content", "create-product");
        return "home"; // Redireciona para o layout principal
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setColors(product.getColors());
        dto.setSizes(product.getSizes());

        // Converter imagens para URLs
        List<String> imageUrls = product.getImages()
                .stream()
                .map(image -> "/products/" + product.getId() + "/images/" + image.getId())
                .collect(Collectors.toList());
        dto.setImageUrls(imageUrls);

        return dto;
    }
}
