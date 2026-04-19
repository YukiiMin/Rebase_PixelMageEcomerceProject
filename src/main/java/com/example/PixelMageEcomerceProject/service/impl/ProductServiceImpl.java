package com.example.PixelMageEcomerceProject.service.impl;

import com.example.PixelMageEcomerceProject.dto.response.ProductResponse;
import com.example.PixelMageEcomerceProject.mapper.ProductMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

import com.example.PixelMageEcomerceProject.enums.ProductType;

import com.example.PixelMageEcomerceProject.dto.request.ProductRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Product;
import com.example.PixelMageEcomerceProject.entity.PackCategory;
import com.example.PixelMageEcomerceProject.entity.CardTemplate;
import com.example.PixelMageEcomerceProject.repository.ProductRepository;
import com.example.PixelMageEcomerceProject.repository.PackCategoryRepository;
import com.example.PixelMageEcomerceProject.repository.CardTemplateRepository;
import com.example.PixelMageEcomerceProject.repository.PackRepository;
import com.example.PixelMageEcomerceProject.repository.CardRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.ProductService;
import com.example.PixelMageEcomerceProject.mapper.CardTemplateMapper;
import com.example.PixelMageEcomerceProject.enums.PackStatus;
import com.example.PixelMageEcomerceProject.enums.CardProductStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final PackCategoryRepository packCategoryRepository;
    private final CardTemplateRepository cardTemplateRepository;
    private final PackRepository packRepository;
    private final CardRepository cardRepository;
    private final ProductMapper productMapper;
    private final CardTemplateMapper cardTemplateMapper;

    private ProductResponse mapToEnrichedResponse(Product product) {
        ProductResponse res = productMapper.toProductResponse(product);
        if (product.getProductType() == ProductType.GACHA_PACK && product.getPackCategory() != null) {
            PackCategory pk = product.getPackCategory();
            res.setPoolSize(pk.getCardsPerPack());
            res.setStockCount((int) packRepository.countByPackCategory_PackCategoryIdAndStatus(pk.getPackCategoryId(), PackStatus.STOCKED));
            if (pk.getCardPools() != null) {
                res.setPoolPreview(pk.getCardPools().stream().map(cardTemplateMapper::toSummaryResponse).toList());
            }
        } else if (product.getProductType() == ProductType.SINGLE_CARD && product.getCardTemplate() != null) {
            CardTemplate ct = product.getCardTemplate();
            res.setPoolSize(1);
            res.setStockCount((int) cardRepository.countByCardTemplate_CardTemplateIdAndStatus(ct.getCardTemplateId(), CardProductStatus.READY));
            res.setPoolPreview(List.of(cardTemplateMapper.toSummaryResponse(ct)));
        }
        return res;
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "products",     allEntries = true),
        @CacheEvict(value = "products-public", allEntries = true),
        @CacheEvict(value = "product-by-id", allEntries = true)
    })
    public ProductResponse createProduct(ProductRequestDTO productRequestDTO) {
        Product product = new Product();
        product.setName(productRequestDTO.getName());
        product.setDescription(productRequestDTO.getDescription());
        product.setPrice(productRequestDTO.getPrice());
        product.setImageUrl(productRequestDTO.getImageUrl());
        
        if (productRequestDTO.getProductType() != null) {
            product.setProductType(productRequestDTO.getProductType());
        }
        
        if (productRequestDTO.getIsVisible() != null) {
            product.setIsVisible(productRequestDTO.getIsVisible());
        }
        
        if (productRequestDTO.getIsActive() != null) {
            product.setIsActive(productRequestDTO.getIsActive());
        }

        if (productRequestDTO.getPackCategoryId() != null) {
            PackCategory pk = packCategoryRepository.findById(productRequestDTO.getPackCategoryId())
                .orElseThrow(() -> new RuntimeException("Pack Category not found"));
            product.setPackCategory(pk);
            
            // Fallback for name, description, imageUrl if empty
            if (product.getName() == null || product.getName().isBlank()) product.setName(pk.getName());
            if (product.getDescription() == null || product.getDescription().isBlank()) product.setDescription(pk.getDescription());
            if (product.getImageUrl() == null || product.getImageUrl().isBlank()) product.setImageUrl(pk.getImageUrl());
        }
        
        if (productRequestDTO.getCardTemplateId() != null) {
            CardTemplate ct = cardTemplateRepository.findById(productRequestDTO.getCardTemplateId())
                .orElseThrow(() -> new RuntimeException("Card Template not found"));
            product.setCardTemplate(ct);
            
            // Fallback for name, description, imageUrl if empty
            if (product.getName() == null || product.getName().isBlank()) product.setName(ct.getName());
            if (product.getDescription() == null || product.getDescription().isBlank()) product.setDescription(ct.getDescription());
            if (product.getImageUrl() == null || product.getImageUrl().isBlank()) product.setImageUrl(ct.getImagePath());
        }

        validateProductBeforeSave(product);

        Product savedProduct = productRepository.save(product);
        
        // Auto-generate initial stock for SINGLE_CARD
        if (savedProduct.getProductType() == ProductType.SINGLE_CARD && 
            savedProduct.getCardTemplate() != null && 
            productRequestDTO.getInitialStock() != null && 
            productRequestDTO.getInitialStock() > 0) {
            
            for (int i = 0; i < productRequestDTO.getInitialStock(); i++) {
                com.example.PixelMageEcomerceProject.entity.Card card = new com.example.PixelMageEcomerceProject.entity.Card();
                card.setCardTemplate(savedProduct.getCardTemplate());
                card.setProduct(savedProduct);
                card.setCardCondition(com.example.PixelMageEcomerceProject.enums.CardCondition.NEW);
                // Set explicitly to READY so it shows up as stock immediately on the marketplace
                card.setStatus(CardProductStatus.READY); 
                card.setSerialNumber("PKG-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                cardRepository.save(card);
            }
        }
        
        return mapToEnrichedResponse(savedProduct);
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "products",      allEntries = true),
        @CacheEvict(value = "products-public", allEntries = true),
        @CacheEvict(value = "product-by-id", key = "#id")
    })
    public ProductResponse updateProduct(Integer id, ProductRequestDTO productRequestDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (productRequestDTO.getName() != null)
            existingProduct.setName(productRequestDTO.getName());
        if (productRequestDTO.getDescription() != null)
            existingProduct.setDescription(productRequestDTO.getDescription());
        if (productRequestDTO.getPrice() != null)
            existingProduct.setPrice(productRequestDTO.getPrice());
        if (productRequestDTO.getImageUrl() != null)
            existingProduct.setImageUrl(productRequestDTO.getImageUrl());
        if (productRequestDTO.getProductType() != null)
            existingProduct.setProductType(productRequestDTO.getProductType());
        if (productRequestDTO.getIsVisible() != null)
            existingProduct.setIsVisible(productRequestDTO.getIsVisible());
        if (productRequestDTO.getIsActive() != null)
            existingProduct.setIsActive(productRequestDTO.getIsActive());
            
        if (productRequestDTO.getPackCategoryId() != null) {
            PackCategory pk = packCategoryRepository.findById(productRequestDTO.getPackCategoryId())
                .orElseThrow(() -> new RuntimeException("Pack Category not found"));
            existingProduct.setPackCategory(pk);
        }
        
        if (productRequestDTO.getCardTemplateId() != null) {
            CardTemplate ct = cardTemplateRepository.findById(productRequestDTO.getCardTemplateId())
                .orElseThrow(() -> new RuntimeException("Card Template not found"));
            existingProduct.setCardTemplate(ct);
        }
        validateProductBeforeSave(existingProduct);
            
        Product updatedProduct = productRepository.save(existingProduct);
        return mapToEnrichedResponse(updatedProduct);
    }

    private void validateProductBeforeSave(Product product) {
        if (product.getName() == null || product.getName().isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống.");
        }
        if (product.getPrice() == null) {
            throw new IllegalArgumentException("Giá sản phẩm không được để trống.");
        }
        if (product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Giá sản phẩm không được âm.");
        }
        if (product.getProductType() == null) {
            throw new IllegalArgumentException("Loại sản phẩm (ProductType) không được để trống.");
        }
        if (product.getProductType() == ProductType.GACHA_PACK && product.getPackCategory() == null) {
            throw new IllegalArgumentException("Sản phẩm loại GACHA_PACK bắt buộc phải có Pack Category.");
        }
        if (product.getProductType() == ProductType.SINGLE_CARD && product.getCardTemplate() == null) {
            throw new IllegalArgumentException("Sản phẩm loại SINGLE_CARD bắt buộc phải có Card Template.");
        }
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "products",      allEntries = true),
        @CacheEvict(value = "products-public", allEntries = true),
        @CacheEvict(value = "product-by-id", key = "#id")
    })
    public void deleteProduct(Integer id) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        existingProduct.setIsActive(false);
        productRepository.save(existingProduct);
    }

    @Override
    @Cacheable(value = "product-by-id", key = "#id")
    public ProductResponse getProductById(Integer id) {
        return productRepository.findById(id)
                .map(this::mapToEnrichedResponse)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    @Cacheable(value = "products")
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToEnrichedResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "products-public")
    public List<ProductResponse> getPublicProducts() {
        // Chỉ trả về sản phẩm có isVisible=true và isActive=true cho khách hàng, 
        // VÀ các thành phần con của nó (Pack Category / Card Template) cũng phải đang hiển thị
        return productRepository.findAllByIsVisibleTrueAndIsActiveTrue().stream()
                .filter(p -> {
                    if (p.getProductType() == ProductType.GACHA_PACK) {
                        return p.getPackCategory() != null && p.getPackCategory().getIsActive();
                    } else if (p.getProductType() == ProductType.SINGLE_CARD) {
                        return p.getCardTemplate() != null && p.getCardTemplate().getActive() && p.getCardTemplate().getIsVisible();
                    }
                    return false;
                })
                .map(this::mapToEnrichedResponse)
                .toList();
    }

    @Override
    public ProductResponse getProductByName(String name) {
        return productRepository.findByName(name)
                .map(this::mapToEnrichedResponse)
                .orElseThrow(() -> new RuntimeException("Product not found with name: " + name));
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "products",        allEntries = true),
        @CacheEvict(value = "products-public",  allEntries = true),
        @CacheEvict(value = "product-by-id",   key = "#id")
    })
    public ProductResponse toggleVisibility(Integer id) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        existingProduct.setIsVisible(existingProduct.getIsVisible() == null || !existingProduct.getIsVisible());
        return mapToEnrichedResponse(productRepository.save(existingProduct));
    }

    @Override
    @Caching(evict = {
        @CacheEvict(value = "products",        allEntries = true),
        @CacheEvict(value = "products-public",  allEntries = true),
        @CacheEvict(value = "product-by-id",   key = "#id")
    })
    public ProductResponse toggleActive(Integer id) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        existingProduct.setIsActive(existingProduct.getIsActive() == null || !existingProduct.getIsActive());
        return mapToEnrichedResponse(productRepository.save(existingProduct));
    }
}
