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
    ProductResponse getProductByName(String name);
}

