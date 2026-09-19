package com.kidsfinance.wallet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository
        extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByChild_Id(Long childProfileId);

    boolean existsByChild_Id(Long childProfileId);
}