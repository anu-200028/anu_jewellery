package com.example.anu_jewellery.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.anu_jewellery.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}