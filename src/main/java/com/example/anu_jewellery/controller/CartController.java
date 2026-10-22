package com.example.anu_jewellery.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.anu_jewellery.dto.AddToCartRequest;
import com.example.anu_jewellery.dto.UpdateCartRequest;
import com.example.anu_jewellery.service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, String>> addToCart(@RequestParam Long productId) {
        String message = cartService.addToCart(productId); 
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping
    public ResponseEntity<?> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    // NEW: Check if current cart is empty
    @GetMapping("/is-empty")
    public ResponseEntity<Boolean> isCartEmpty() {
        return ResponseEntity.ok(cartService.isCartEmpty());
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<Map<String, String>> updateCartItem(@PathVariable Long cartItemId,
                                                              @Valid @RequestBody UpdateCartRequest request) {
        String message = cartService.updateCartItem(cartItemId, request);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Map<String, String>> removeCartItem(@PathVariable Long cartItemId) {
        String message = cartService.removeCartItem(cartItemId);
        return ResponseEntity.ok(Map.of("message", message));
    }
}