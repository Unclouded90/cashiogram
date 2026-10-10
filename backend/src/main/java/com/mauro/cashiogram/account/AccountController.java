package com.mauro.cashiogram.account;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mauro.cashiogram.account.dto.AccountResponseDTO;
import com.mauro.cashiogram.account.dto.CreateAccountDTO;
import com.mauro.cashiogram.account.dto.UpdateAccountDTO;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<AccountResponseDTO> getAccount() {
        return ResponseEntity.ok(accountService.getAccount());
    }

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(
            @Valid @RequestBody CreateAccountDTO newAccount) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountService.createAccount(newAccount));
    }

    @PutMapping
    public ResponseEntity<AccountResponseDTO> updateAccount(@Valid @RequestBody UpdateAccountDTO updateAccount) {

        return ResponseEntity.ok(accountService.updateAccount(updateAccount));
    }
}
