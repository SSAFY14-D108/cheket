package com.ssafy.cheket.repository.wallet;

import com.ssafy.cheket.entity.wallet.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
}
