package com.kidsfinance.wallet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository
        extends JpaRepository<WalletTransaction, Long> {

    List<WalletTransaction> findAllByWallet_IdOrderByCreatedAtDescIdDesc(
            Long walletId
    );
}