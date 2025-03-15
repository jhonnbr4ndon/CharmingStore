package com.charmosaPlus.Charmosa.Service;

import com.charmosaPlus.Charmosa.Repository.CouponRepository;
import com.charmosaPlus.Charmosa.Repository.ProductRepository;
import com.charmosaPlus.Charmosa.domain.Coupon;
import com.charmosaPlus.Charmosa.domain.Product;
import com.charmosaPlus.Charmosa.domain.dto.CouponDTO;
import com.charmosaPlus.Charmosa.domain.exception.CouponException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
        // 🔍 Verifica se já existe um cupom com o mesmo código
        if (couponRepository.existsByCode(couponDTO.getCode())) {
            throw new CouponException("Cupom com o código '" + couponDTO.getCode() + "' já existe!");
        }

        Coupon coupon = new Coupon();
        coupon.setCode(couponDTO.getCode());
        coupon.setDiscountPercentage(couponDTO.getDiscountPercentage());
        coupon.setExpirationDate(couponDTO.getExpirationDate()); // Definindo data de expiração

        if (couponDTO.getProductId() != null) {
            Product product = productRepository.findById(couponDTO.getProductId())
                    .orElseThrow(() -> new CouponException("Produto não encontrado para o cupom"));
            coupon.setProduct(product);
        }

        coupon = couponRepository.save(coupon);
        return convertToDTO(coupon);
    }


    // Método para deletar cupons expirados automaticamente
    @Scheduled(cron = "0 0 0 * * ?") // Executa todo dia à meia-noite
    @Transactional
    public void removeExpiredCoupons() {
        List<Coupon> expiredCoupons = couponRepository.findAll().stream()
                .filter(coupon -> coupon.getExpirationDate() != null && coupon.getExpirationDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());

        couponRepository.deleteAll(expiredCoupons);
    }

    // Converter entidade para DTO
    private CouponDTO convertToDTO(Coupon coupon) {
        CouponDTO dto = new CouponDTO();
        dto.setId(coupon.getId());
        dto.setCode(coupon.getCode());
        dto.setDiscountPercentage(coupon.getDiscountPercentage());
        dto.setProductId(coupon.getProduct() != null ? coupon.getProduct().getId() : null);
        dto.setExpirationDate(coupon.getExpirationDate());
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