package com.ssafy.cheket.controller.wallet;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.wallet.response.TransactionResponse;
import com.ssafy.cheket.dto.wallet.response.WalletBalanceResponse;
import com.ssafy.cheket.service.wallet.TransactionService;
import com.ssafy.cheket.service.wallet.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;
    private final TransactionService transactionService;

    @GetMapping("/balance")
    @Operation(summary = "CTK 잔액 조회 (DB)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> getBalance(@AuthenticationPrincipal Long userId) {
        WalletBalanceResponse response = walletService.getBalance(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "잔액 조회 성공", response));
    }

    @GetMapping("/balance/refresh")
    @Operation(summary = "CTK 잔액 새로고침 (온체인 조회)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<WalletBalanceResponse>> refreshBalance(@AuthenticationPrincipal Long userId) {
        WalletBalanceResponse response = walletService.refreshBalance(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "잔액 새로고침 성공", response));
    }

    @GetMapping("/transactions")
    @Operation(summary = "거래 내역 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(@AuthenticationPrincipal Long userId,
        @RequestParam(required = false) String type) {
        List<TransactionResponse> response = transactionService.getTransactions(userId, type);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "거래 내역 조회 성공", response));
    }
}
