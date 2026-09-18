package com.kidsfinance.family.dto.response;

import java.time.OffsetDateTime;

public record ParentChildLinkResponse(
        Long id,
        Long parentProfileId,
        Long childProfileId,
        OffsetDateTime createdAt
) {
}