package com.charmosaPlus.Charmosa.Service;

import com.charmosaPlus.Charmosa.Repository.CartItemRepository;
import com.charmosaPlus.Charmosa.Repository.CartRepository;
import com.charmosaPlus.Charmosa.Repository.CouponRepository;
import com.charmosaPlus.Charmosa.Repository.ProductRepository;
import com.charmosaPlus.Charmosa.domain.Cart;
import com.charmosaPlus.Charmosa.domain.CartItem;
import com.charmosaPlus.Charmosa.domain.Coupon;
import com.charmosaPlus.Charmosa.domain.Product;
import com.charmosaPlus.Charmosa.domain.dto.CartDTO;
import com.charmosaPlus.Charmosa.domain.dto.CartItemDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductRepository productRepository, CouponRepository couponRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.couponRepository = couponRepository;
    }

    @Transactional
    public CartDTO applyCouponToCart(String code) {
        Cart cart = getOrCreateCart();
        Optional<Coupon> optionalCoupon = couponRepository.findByCode(code);

        if (optionalCoupon.isEmpty()) {
            throw new RuntimeException("Cupom inválido ou não encontrado");
        }

        Coupon coupon = optionalCoupon.get();

        // Verifica se o cupom está expirado
        if (coupon.getExpirationDate() != null && coupon.getExpirationDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Este cupom já expirou!");
        }

        double totalDiscount = 0.0;

        for (CartItem item : cart.getItems()) {
            boolean appliesToProduct = coupon.getProduct() == null ||
                    coupon.getProduct().getId().equals(item.getProduct().getId());

            if (appliesToProduct) {
                double discount = (item.getProduct().getPrice() * item.getQuantity()) * (coupon.getDiscountPercentage() / 100);
                totalDiscount += discount;
            }
        }

        // Converte o carrinho para DTO
        CartDTO cartDTO = convertToCartDTO(cart);

        // Aplica o desconto ao valor total do carrinho
        cartDTO.setTotalAmount(cartDTO.getTotalAmount() - totalDiscount);

        // Define o valor que o usuário economizou
        cartDTO.setDiscountAmount(totalDiscount);

        return cartDTO;
    }

    @Transactional
    public Cart getOrCreateCart() {
        return cartRepository.findAll().stream().findFirst().orElseGet(() -> cartRepository.save(new Cart()));
    }

    @Transactional
    public CartDTO addItemToCart(CartItemDTO cartItemDTO) {
        if (cartItemDTO.getProductId() == null) {
            throw new IllegalArgumentException("O ID do produto não pode ser nulo");
        }

        Cart cart = getOrCreateCart();
        Product product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        // Verifica se a quantidade desejada está disponível no estoque
        if (product.getQuantity() < cartItemDTO.getQuantity()) {
            throw new RuntimeException("Quantidade insuficiente no estoque!");
        }

        // Verifica se o item já está no carrinho
        for (CartItem item : cart.getItems()) {
            if (item.getProduct().getId().equals(product.getId()) &&
                    item.getSelectedColor().equals(cartItemDTO.getSelectedColor()) &&
                    item.getSelectedSize().equals(cartItemDTO.getSelectedSize())) {

                // Atualiza a quantidade e reduz do estoque
                item.setQuantity(item.getQuantity() + cartItemDTO.getQuantity());
                product.setQuantity(product.getQuantity() - cartItemDTO.getQuantity());

                productRepository.save(product);
                cartRepository.save(cart);
                return convertToCartDTO(cart);
            }
        }

        // Criar novo item no carrinho
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setSelectedColor(cartItemDTO.getSelectedColor());
        cartItem.setSelectedSize(cartItemDTO.getSelectedSize());
        cartItem.setQuantity(cartItemDTO.getQuantity());

        // Reduzir o estoque do produto
        product.setQuantity(product.getQuantity() - cartItemDTO.getQuantity());

        cart.getItems().add(cartItem);
        productRepository.save(product);
        cartRepository.save(cart);

        return convertToCartDTO(cart);
    }

    private CartDTO convertToCartDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());

        List<CartItemDTO> itemsDTO = cart.getItems().stream().map(item -> {
            CartItemDTO itemDTO = new CartItemDTO();

            itemDTO.setId(item.getId());
            itemDTO.setProductId(item.getProduct().getId());
            itemDTO.setSelectedColor(item.getSelectedColor());
            itemDTO.setSelectedSize(item.getSelectedSize());
            itemDTO.setQuantity(item.getQuantity());

            // Adicionando preço unitário do produto
            itemDTO.setUnitPrice(item.getProduct().getPrice());

            // Calculando preço total baseado na quantidade
            itemDTO.setTotalPrice(item.getProduct().getPrice() * item.getQuantity());

            return itemDTO;
        }).collect(Collectors.toList());

        cartDTO.setItems(itemsDTO);

        // Calculando o total sem desconto
        double totalAmount = itemsDTO.stream()
                .mapToDouble(CartItemDTO::getTotalPrice)
                .sum();

        cartDTO.setTotalAmount(totalAmount);
        cartDTO.setDiscountAmount(0.0); // Inicializa com 0, caso não tenha cupom aplicado

        return cartDTO;
    }

    @Transactional
    public void removeItemFromCart(Long itemId) {
        Cart cart = getOrCreateCart();

        CartItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item do carrinho não encontrado"));

        // Devolve a quantidade ao estoque do produto
        Product product = itemToRemove.getProduct();
        product.setQuantity(product.getQuantity() + itemToRemove.getQuantity());
        productRepository.save(product);

        // Remove o item do carrinho
        cart.getItems().remove(itemToRemove);
        cartItemRepository.deleteById(itemId);
    }

    @Transactional
    public void clearCart() {
        cartRepository.deleteAll();
    }

    @Transactional
    public CartDTO getCartDTO() {
        return convertToCartDTO(getOrCreateCart());
    }
}
