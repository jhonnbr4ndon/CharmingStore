package com.charmosaPlus.Charmosa.controller;

import com.charmosaPlus.Charmosa.Service.CartService;
import com.charmosaPlus.Charmosa.Service.CouponService;
import com.charmosaPlus.Charmosa.domain.Coupon;
import com.charmosaPlus.Charmosa.domain.dto.CartDTO;
import com.charmosaPlus.Charmosa.domain.dto.CartItemDTO;
import com.charmosaPlus.Charmosa.domain.dto.CouponDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CouponService couponService;

    public CartController(CartService cartService, CouponService couponService) {
        this.cartService = cartService;
        this.couponService = couponService;
    }

    @GetMapping
    public ResponseEntity<CartDTO> getCart() {
        return ResponseEntity.ok(cartService.getCartDTO());
    }

    @PostMapping("/add")
    public ResponseEntity<CartDTO> addItemToCart(@RequestBody CartItemDTO cartItemDTO) {
        return ResponseEntity.ok(cartService.addItemToCart(cartItemDTO));
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<Void> removeItemFromCart(@PathVariable Long itemId) {
        cartService.removeItemFromCart(itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }

    // Endpoint para listar todos os cupons criados
    @GetMapping("/coupons")
    public ResponseEntity<List<CouponDTO>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    // Aplicar cupom ao carrinho
    @PostMapping("/apply-coupon/{code}")
    public ResponseEntity<CartDTO> applyCoupon(@PathVariable String code) {
        return ResponseEntity.ok(cartService.applyCouponToCart(code));
    }

    // Criar um cupom
    @PostMapping("/coupon")
    public ResponseEntity<CouponDTO> createCoupon(@RequestBody CouponDTO couponDTO) {
        return ResponseEntity.ok(couponService.createCoupon(couponDTO));
    }

    @DeleteMapping("/coupon/{couponId}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long couponId) {
        couponService.deleteCoupon(couponId);
        return ResponseEntity.noContent().build();
    }
}
