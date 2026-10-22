package com.example.anu_jewellery.dto;

public record AuthResponse(
        String token,
        String tokenType,
        String username,
        String role
) {
}