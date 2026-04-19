package com.example.PixelMageEcomerceProject.service.interfaces;

import com.example.PixelMageEcomerceProject.dto.request.ProductRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(ProductRequestDTO productRequestDTO);
    ProductResponse updateProduct(Integer id, ProductRequestDTO productRequestDTO);
    void deleteProduct(Integer id);
    ProductResponse getProductById(Integer id);
    List<ProductResponse> getAllProducts();
    List<ProductResponse> getPublicProducts(); // chỉ trả isVisible=true & isActive=true
    ProductResponse getProductByName(String name);
    ProductResponse toggleVisibility(Integer id);
    ProductResponse toggleActive(Integer id);
}

