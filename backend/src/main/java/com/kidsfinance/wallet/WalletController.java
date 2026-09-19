package com.kidsfinance.wallet;

import com.kidsfinance.wallet.dto.response.WalletResponse;
import com.kidsfinance.wallet.dto.response.WalletTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parents/me/children")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{childId}/wallet")
    public WalletResponse getChildWallet(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long childId
    ) {

        Long parentUserId =
                Long.parseLong(jwt.getSubject());

        return walletService.getWalletForParent(
                parentUserId,
                childId
        );
    }

    @GetMapping("/{childId}/wallet/transactions")
    public List<WalletTransactionResponse> getChildWalletTransactions(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long childId
    ) {

        Long parentUserId =
                Long.parseLong(jwt.getSubject());

        return walletService.getTransactionsForParent(
                parentUserId,
                childId
        );
    }
}