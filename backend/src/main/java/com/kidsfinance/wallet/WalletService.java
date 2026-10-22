package com.kidsfinance.wallet;

import com.kidsfinance.child.ChildProfile;
import com.kidsfinance.child.ChildProfileRepository;
import com.kidsfinance.common.exception.ConflictException;
import com.kidsfinance.common.exception.ResourceNotFoundException;
import com.kidsfinance.family.ParentChildLinkRepository;
import com.kidsfinance.wallet.dto.response.WalletResponse;
import com.kidsfinance.wallet.dto.response.WalletTransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    private final ChildProfileRepository childProfileRepository;

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

        Wallet savedWallet =
                walletRepository.save(wallet);

        WalletTransaction initialTransaction =
                WalletTransaction.builder()
                        .wallet(savedWallet)
                        .type(WalletTransactionType.INITIAL_CREDIT)
                        .amount(INITIAL_BALANCE)
                        .balanceAfter(INITIAL_BALANCE)
                        .description("Initial wallet balance")
                        .build();

        walletTransactionRepository.save(
                initialTransaction
        );

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

        return getWalletResponse(
                childProfileId
        );
    }

    @Transactional(readOnly = true)
    public WalletResponse getWalletForChild(
            Long childUserId
    ) {

        ChildProfile child =
                getChildByUserId(childUserId);

        return getWalletResponse(
                child.getId()
        );
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

        Wallet wallet =
                getWallet(childProfileId);

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

        Wallet wallet =
                getWalletForUpdate(childProfileId);

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

        return toTransactionResponse(
                walletTransactionRepository.save(
                        transaction
                )
        );
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

        return debitInternal(
                childProfileId,
                amount,
                description
        );
    }

    /*
     * Internal business operation used by the simulation engine.
     *
     * Authorization must already have been performed by the caller.
     * Requiring an existing transaction ensures wallet debit and
     * simulation decision are committed or rolled back together.
     */
    @Transactional(
            propagation = Propagation.MANDATORY
    )
    public WalletTransactionResponse debitForSimulation(
            Long childProfileId,
            BigDecimal amount,
            String description
    ) {

        return debitInternal(
                childProfileId,
                amount,
                description
        );
    }

    private WalletTransactionResponse debitInternal(
            Long childProfileId,
            BigDecimal amount,
            String description
    ) {

        Wallet wallet =
                getWalletForUpdate(childProfileId);

        if (wallet.getBalance()
                .compareTo(amount) < 0) {

            throw new ConflictException(
                    "Insufficient wallet balance"
            );
        }

        BigDecimal newBalance =
                wallet.getBalance()
                        .subtract(amount);

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
                walletTransactionRepository
                        .save(transaction);

        return toTransactionResponse(
                savedTransaction
        );
    }

    private ChildProfile getChildByUserId(
            Long childUserId
    ) {

        return childProfileRepository
                .findByUser_Id(childUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Child profile not found for authenticated user"
                        )
                );
    }

    private WalletResponse getWalletResponse(
            Long childProfileId
    ) {

        return toWalletResponse(
                getWallet(childProfileId)
        );
    }

    private Wallet getWallet(
            Long childProfileId
    ) {

        return walletRepository
                .findByChild_Id(childProfileId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Wallet not found for child: "
                                        + childProfileId
                        )
                );
    }

    private Wallet getWalletForUpdate(
            Long childProfileId
    ) {

        return walletRepository
                .findByChildIdForUpdate(
                        childProfileId
                )
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

    private WalletResponse toWalletResponse(
            Wallet wallet
    ) {

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

    @Transactional(readOnly = true)
    public List<WalletTransactionResponse> getTransactionsForChild(
            Long childUserId
    ) {

        ChildProfile child =
                getChildByUserId(childUserId);

        Wallet wallet =
                getWallet(child.getId());

        return walletTransactionRepository
                .findAllByWallet_IdOrderByCreatedAtDescIdDesc(
                        wallet.getId()
                )
                .stream()
                .map(this::toTransactionResponse)
                .toList();
    }
}