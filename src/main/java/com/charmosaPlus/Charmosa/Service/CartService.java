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
import com.charmosaPlus.Charmosa.domain.enums.DiscountType;
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

        // 1. Verificar expiração
        if (coupon.getExpirationDate() != null && coupon.getExpirationDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Este cupom já expirou!");
        }

        // 2. Verificar quantidade máxima de uso
        if (coupon.getMaxUses() != null && coupon.getTimesUsed() >= coupon.getMaxUses()) {
            throw new RuntimeException("Este cupom já foi utilizado no limite permitido.");
        }


        // 3. Calcular valor total do carrinho
        double totalAmount = cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        // 4. Verificar valor mínimo para aplicar
        if (coupon.getMinimumAmountToApply() != null && totalAmount < coupon.getMinimumAmountToApply()) {
            throw new RuntimeException("Valor mínimo para aplicar o cupom não atingido: R$ " + coupon.getMinimumAmountToApply());
        }

        // 5. Calcular desconto
        double totalDiscount = 0.0;

        for (CartItem item : cart.getItems()) {
            boolean appliesToProduct = coupon.getProduct() == null ||
                    (item.getProduct().getId().equals(coupon.getProduct().getId()));

            if (appliesToProduct) {
                double itemTotal = item.getProduct().getPrice() * item.getQuantity();

                if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
                    totalDiscount += itemTotal * (coupon.getDiscountValue() / 100);
                } else if (coupon.getDiscountType() == DiscountType.FIXED) {
                    // Aplica o valor fixo apenas uma vez (em qualquer item aplicável)
                    totalDiscount += coupon.getDiscountValue();
                    break; // Apenas uma vez para tipo FIXED
                }
            }
        }

        // Garante que o desconto não seja maior que o valor total
        if (totalDiscount > totalAmount) {
            totalDiscount = totalAmount;
        }

        // 6. Incrementa o uso do cupom
        coupon.setTimesUsed(coupon.getTimesUsed() + 1);
        couponRepository.save(coupon);

        // 7. Converte o carrinho para DTO
        CartDTO cartDTO = convertToCartDTO(cart);
        cartDTO.setDiscountAmount(totalDiscount);
        cartDTO.setTotalAmount(cartDTO.getTotalAmount() - totalDiscount);

        // (Opcional) Frete grátis pode ser marcado aqui também
        if (coupon.getFreeShipping() != null && coupon.getFreeShipping()) {
            // você pode definir um campo `freeShipping` no CartDTO se quiser
            // cartDTO.setFreeShipping(true);
        }

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

    @Transactional
    public CartDTO updateItemQuantity(Long itemId, int newQuantity) {
        Cart cart = getOrCreateCart();

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item não encontrado no carrinho"));

        Product product = item.getProduct();
        int currentQuantity = item.getQuantity();
        int diff = newQuantity - currentQuantity;

        if (diff > 0) {
            if (product.getQuantity() < diff) {
                throw new RuntimeException("Estoque insuficiente");
            }
            product.setQuantity(product.getQuantity() - diff);
        } else {
            product.setQuantity(product.getQuantity() + Math.abs(diff));
        }

        item.setQuantity(newQuantity);

        productRepository.save(product);
        cartRepository.save(cart);

        return convertToCartDTO(cart);
    }

    private String getFirstImageUrl(Product product) {
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            Long productId = product.getId();
            Long imageId = product.getImages().get(0).getId();
            return "/products/" + productId + "/images/" + imageId;
        }
        return "/images/default.jpg";
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

            itemDTO.setAvailableStock(item.getProduct().getQuantity());
            itemDTO.setImageUrl(getFirstImageUrl(item.getProduct()));

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
