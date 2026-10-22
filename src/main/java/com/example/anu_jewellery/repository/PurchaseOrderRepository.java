package com.example.anu_jewellery.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.anu_jewellery.entity.PurchaseOrder;
import com.example.anu_jewellery.entity.User;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    
    Optional<PurchaseOrder> findByIdAndUser(Long id, User user);
    
    Optional<PurchaseOrder> findByRazorpayOrderId(String razorpayOrderId);
}