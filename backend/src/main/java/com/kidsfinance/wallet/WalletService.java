package com.kidsfinance.wallet;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.common.exception.ConflictException;
import com.kidsfinance.common.exception.ResourceNotFoundException;
import com.kidsfinance.family.ParentChildLinkRepository;
import com.kidsfinance.wallet.dto.response.WalletResponse;
import com.kidsfinance.wallet.dto.response.WalletTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private static final BigDecimal INITIAL_BALANCE =
            new BigDecimal("1000.00");

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final ParentChildLinkRepository parentChildLinkRepository;

    @Transactional
    public Wallet createInitialWallet(ChildProfile child) {

        if (walletRepository.existsByChild_Id(child.getId())) {
            throw new ConflictException(
                    "Wallet already exists for child: " + child.getId()
            );
        }

        Wallet wallet = Wallet.builder()
                .child(child)
                .balance(INITIAL_BALANCE)
                .currency(WalletCurrency.AZN)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);

        WalletTransaction initialTransaction =
                WalletTransaction.builder()
                        .wallet(savedWallet)
                        .type(WalletTransactionType.INITIAL_CREDIT)
                        .amount(INITIAL_BALANCE)
                        .balanceAfter(INITIAL_BALANCE)
                        .description("Initial wallet balance")
                        .build();

        walletTransactionRepository.save(initialTransaction);

        return savedWallet;
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletForParent(
            Long parentUserId,
            Long childProfileId
    ) {

        verifyChildBelongsToParent(
                parentUserId,
                childProfileId
        );

        Wallet wallet = walletRepository
                .findByChild_Id(childProfileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Wallet not found for child: "
                                        + childProfileId
                        )
                );

        return toWalletResponse(wallet);
    }

    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getTransactionsForParent(
            Long parentUserId,
            Long childProfileId
    ) {

        verifyChildBelongsToParent(
                parentUserId,
                childProfileId
        );

        Wallet wallet = walletRepository
                .findByChild_Id(childProfileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Wallet not found for child: "
                                        + childProfileId
                        )
                );

        return walletTransactionRepository
                .findAllByWallet_IdOrderByCreatedAtDescIdDesc(
                        wallet.getId()
                )
                .stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    @Transactional
    public WalletTransactionResponse credit(
            Long parentUserId,
            Long childProfileId,
            BigDecimal amount,
            String description
    ) {

        verifyChildBelongsToParent(
                parentUserId,
                childProfileId
        );

        Wallet wallet = getWalletForUpdate(childProfileId);

        BigDecimal newBalance =
                wallet.getBalance().add(amount);

        wallet.setBalance(newBalance);

        WalletTransaction transaction =
                WalletTransaction.builder()
                        .wallet(wallet)
                        .type(WalletTransactionType.CREDIT)
                        .amount(amount)
                        .balanceAfter(newBalance)
                        .description(description)
                        .build();

        WalletTransaction savedTransaction =
                walletTransactionRepository.save(transaction);

        return toTransactionResponse(savedTransaction);
    }

    @Transactional
    public WalletTransactionResponse debit(
            Long parentUserId,
            Long childProfileId,
            BigDecimal amount,
            String description
    ) {

        verifyChildBelongsToParent(
                parentUserId,
                childProfileId
        );

        Wallet wallet = getWalletForUpdate(childProfileId);

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new ConflictException(
                    "Insufficient wallet balance"
            );
        }

        BigDecimal newBalance =
                wallet.getBalance().subtract(amount);

        wallet.setBalance(newBalance);

        WalletTransaction transaction =
                WalletTransaction.builder()
                        .wallet(wallet)
                        .type(WalletTransactionType.DEBIT)
                        .amount(amount)
                        .balanceAfter(newBalance)
                        .description(description)
                        .build();

        WalletTransaction savedTransaction =
                walletTransactionRepository.save(transaction);

        return toTransactionResponse(savedTransaction);
    }

    private Wallet getWalletForUpdate(Long childProfileId) {

        return walletRepository
                .findByChildIdForUpdate(childProfileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Wallet not found for child: "
                                        + childProfileId
                        )
                );
    }

    private void verifyChildBelongsToParent(
            Long parentUserId,
            Long childProfileId
    ) {

        boolean belongsToParent =
                parentChildLinkRepository
                        .existsByParent_User_IdAndChild_Id(
                                parentUserId,
                                childProfileId
                        );

        if (!belongsToParent) {
            throw new ResourceNotFoundException(
                    "Child not found for authenticated parent"
            );
        }
    }

    private WalletResponse toWalletResponse(Wallet wallet) {

        return new WalletResponse(
                wallet.getId(),
                wallet.getChild().getId(),
                wallet.getBalance(),
                wallet.getCurrency()
        );
    }

    private WalletTransactionResponse toTransactionResponse(
            WalletTransaction transaction
    ) {

        return new WalletTransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}