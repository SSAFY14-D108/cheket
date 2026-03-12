package com.ssafy.cheket.repository.wallet;

import com.ssafy.cheket.entity.wallet.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByAddress(String address);
    Optional<Wallet> findByAddressIgnoreCase(String address);

}
