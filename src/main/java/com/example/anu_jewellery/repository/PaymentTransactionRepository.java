package com.example.anu_jewellery.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.anu_jewellery.entity.PaymentTransaction;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    
    // Allows finding an existing transaction by Razorpay's Order ID
    Optional<PaymentTransaction> findByRazorpayOrderId(String razorpayOrderId);
    
    // Allows finding an existing transaction by Razorpay's Payment ID
    Optional<PaymentTransaction> findByRazorpayPaymentId(String razorpayPaymentId);
}