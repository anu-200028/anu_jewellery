package com.example.anu_jewellery.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.anu_jewellery.dto.ProductRequest;
import com.example.anu_jewellery.dto.ProductResponse;
import com.example.anu_jewellery.entity.Product;
import com.example.anu_jewellery.repository.ProductRepository;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public ProductResponse addProduct(ProductRequest request) {
        Product product = new Product();
        mapRequestToEntity(request, product);
        Product savedProduct = productRepository.save(product);
        return mapToResponse(savedProduct);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        mapRequestToEntity(request, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);
        return mapToResponse(updatedProduct);
    }

    public String deleteProduct(Long id) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        productRepository.delete(existingProduct);
        return "Product deleted successfully";
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        return mapToResponse(product);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Helper method: Map Entity to Response DTO
    private ProductResponse mapToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setCategory(product.getCategory());
        response.setMetalType(product.getMetalType());
        response.setGender(product.getGender());
        response.setStoneType(product.getStoneType());
        
        // Mappings for weights
        response.setStoneWeight(product.getStoneWeight());
        response.setWeight(product.getWeight());

        response.setOccasion(product.getOccasion());
        response.setImageUrl(product.getImageUrl());
        return response;
    }

    // Helper method: Map Request DTO to Entity
 // Helper method: Map Request DTO (Record) to Entity
    private void mapRequestToEntity(ProductRequest request, Product product) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setCategory(request.category());
        product.setMetalType(request.metalType());
        product.setGender(request.gender());
        product.setStoneType(request.stoneType());
        product.setStoneWeight(request.stoneWeight());
        product.setWeight(request.weight());
        product.setOccasion(request.occasion());
        product.setImageUrl(request.imageUrl());
    }
}