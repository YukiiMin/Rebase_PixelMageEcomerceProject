package com.example.PixelMageEcomerceProject.service.impl;

import com.example.PixelMageEcomerceProject.dto.response.ProductResponse;
import com.example.PixelMageEcomerceProject.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import com.example.PixelMageEcomerceProject.dto.request.ProductRequestDTO;
import com.example.PixelMageEcomerceProject.entity.Product;
import com.example.PixelMageEcomerceProject.repository.ProductRepository;
import com.example.PixelMageEcomerceProject.service.interfaces.ProductService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse createProduct(ProductRequestDTO productRequestDTO) {
        Product product = new Product();
        product.setName(productRequestDTO.getName());
        product.setDescription(productRequestDTO.getDescription());
        product.setPrice(productRequestDTO.getPrice());
        product.setImageUrl(productRequestDTO.getImageUrl());
        Product savedProduct = productRepository.save(product);
        return productMapper.toProductResponse(savedProduct);
    }

    @Override
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
            
        Product updatedProduct = productRepository.save(existingProduct);
        return productMapper.toProductResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Integer id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public ProductResponse getProductById(Integer id) {
        return productRepository.findById(id)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(productMapper::toProductResponse)
                .toList();
    }

    @Override
    public ProductResponse getProductByName(String name) {
        return productRepository.findByName(name)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new RuntimeException("Product not found with name: " + name));
    }
}
