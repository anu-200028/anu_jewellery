package com.example.anu_jewellery.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.anu_jewellery.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find products by exact category (e.g., "RINGS")
    List<Product> findByCategory(String category);

    // Find products by metal type (e.g., "PLATINUM")
    List<Product> findByMetalType(String metalType);

    // Find products by gender (e.g., "WOMEN")
    List<Product> findByGender(String gender);

    // Filter by both category and gender
    List<Product> findByCategoryAndGender(String category, String gender);

    // Search products containing a keyword in their name (case-insensitive)
    List<Product> findByNameContainingIgnoreCase(String keyword);
}