package com.kidsfinance.parent.dto;

public record ParentResponse(
        Long id,
        Long userId,
        String username,
        String fullName,
        String email
) {
}