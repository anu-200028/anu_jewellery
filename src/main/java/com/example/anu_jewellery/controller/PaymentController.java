package com.example.anu_jewellery.controller;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.anu_jewellery.dto.CreatePaymentOrderResponse;
import com.example.anu_jewellery.dto.VerifyPaymentRequest;
import com.example.anu_jewellery.service.PaymentService;

@RestController
@RequestMapping("/customer/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<CreatePaymentOrderResponse> createOrder() {
        return ResponseEntity.ok(paymentService.createPaymentOrder());
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyPayment(@RequestBody VerifyPaymentRequest request) {
        String message = paymentService.verifyPayment(request);
        return ResponseEntity.ok(Map.of("message", message));
    }
}