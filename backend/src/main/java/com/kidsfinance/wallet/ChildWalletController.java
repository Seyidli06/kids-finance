package com.kidsfinance.wallet;

import com.kidsfinance.wallet.dto.response.WalletResponse;
import com.kidsfinance.wallet.dto.response.WalletTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/children/me")
@RequiredArgsConstructor
public class ChildWalletController {

    private final WalletService walletService;

    @GetMapping("/wallet")
    public WalletResponse getWallet(
            @AuthenticationPrincipal Jwt jwt
    ) {

        return walletService.getWalletForChild(
                getUserId(jwt)
        );
    }

    @GetMapping("/wallet/transactions")
    public List<WalletTransactionResponse> getTransactions(
            @AuthenticationPrincipal Jwt jwt
    ) {

        return walletService.getTransactionsForChild(
                getUserId(jwt)
        );
    }

    private Long getUserId(Jwt jwt) {
        return Long.parseLong(jwt.getSubject());
    }
}