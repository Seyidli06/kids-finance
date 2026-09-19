package com.kidsfinance.wallet.dto.response;

import com.kidsfinance.wallet.WalletCurrency;

import java.math.BigDecimal;

public record WalletResponse(
        Long id,
        Long childProfileId,
        BigDecimal balance,
        WalletCurrency currency
) {
}