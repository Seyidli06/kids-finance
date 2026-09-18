package com.kidsfinance.family.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LinkParentChildRequest(

        @NotNull
        @Positive
        Long parentProfileId,

        @NotNull
        @Positive
        Long childProfileId
) {
}