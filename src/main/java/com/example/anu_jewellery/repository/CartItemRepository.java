package com.example.anu_jewellery.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.anu_jewellery.entity.CartItem;
import com.example.anu_jewellery.entity.Product;
import com.example.anu_jewellery.entity.User;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUserAndProduct(User user, Product product);

    Optional<CartItem> findByIdAndUser(Long id, User user);
    
    void deleteByUser(User user);
}