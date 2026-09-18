package com.kidsfinance.child.dto.response;

public record ChildResponse(
        Long id,
        Long userId,
        String username,
        String displayName,
        Short age,
        Integer xp,
        Integer level
) {
}