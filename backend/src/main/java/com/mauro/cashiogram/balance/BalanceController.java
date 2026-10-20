package com.mauro.cashiogram.balance;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mauro.cashiogram.balance.dto.BalanceResponseDTO;

@RestController
@RequestMapping("/balance")
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping
    public ResponseEntity<BalanceResponseDTO> getBalance() {
        return ResponseEntity.ok(
                balanceService.getBalance());
    }
}