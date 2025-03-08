package com.charmosaPlus.Charmosa.Service;

import com.charmosaPlus.Charmosa.Repository.CouponRepository;
import com.charmosaPlus.Charmosa.Repository.ProductRepository;
import com.charmosaPlus.Charmosa.domain.Coupon;
import com.charmosaPlus.Charmosa.domain.Product;
import com.charmosaPlus.Charmosa.domain.dto.CouponDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;

    public CouponService(CouponRepository couponRepository, ProductRepository productRepository) {
        this.couponRepository = couponRepository;
        this.productRepository = productRepository;
    }

    // Buscar todos os cupons
    public List<CouponDTO> getAllCoupons() {
        return couponRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Criar um novo cupom
    public CouponDTO createCoupon(CouponDTO couponDTO) {
        Coupon coupon = new Coupon();
        coupon.setCode(couponDTO.getCode());
        coupon.setDiscountPercentage(couponDTO.getDiscountPercentage());

        if (couponDTO.getProductId() != null) {
            Product product = productRepository.findById(couponDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
            coupon.setProduct(product);
        }

        coupon = couponRepository.save(coupon);
        return convertToDTO(coupon);
    }

    // Converter entidade para DTO
    private CouponDTO convertToDTO(Coupon coupon) {
        CouponDTO dto = new CouponDTO();
        dto.setId(coupon.getId());
        dto.setCode(coupon.getCode());
        dto.setDiscountPercentage(coupon.getDiscountPercentage());
        dto.setProductId(coupon.getProduct() != null ? coupon.getProduct().getId() : null);
        return dto;
    }

    @Transactional
    public void deleteCoupon(Long couponId) {
        if (!couponRepository.existsById(couponId)) {
            throw new RuntimeException("Cupom não encontrado!");
        }
        couponRepository.deleteById(couponId);
    }
}