package com.kidsfinance.wallet.dto.response;

import com.kidsfinance.wallet.WalletTransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record WalletTransactionResponse(
        Long id,
        WalletTransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        OffsetDateTime createdAt
) {
}