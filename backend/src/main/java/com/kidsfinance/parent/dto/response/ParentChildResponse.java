package com.kidsfinance.parent.dto.response;

public record ParentChildResponse(
        Long id,
        String displayName,
        Short age,
        Integer xp,
        Integer level
) {
}