package com.mauro.cashiogram.account;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mauro.cashiogram.account.dto.AccountResponseDTO;
import com.mauro.cashiogram.account.dto.CreateAccountDTO;
import com.mauro.cashiogram.account.dto.UpdateAccountDTO;
import com.mauro.cashiogram.account.entity.AccountEntity;
import com.mauro.cashiogram.account.repository.AccountRepository;
import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountResponseDTO getAccount() {

        return mapToResponseDTO(getAccountOrThrow());
    }

    @Transactional
    public AccountResponseDTO createAccount(CreateAccountDTO createAccountDTO) {
        if (accountRepository.findFirstByOrderByCreatedAt().isPresent()) {
            throw new BadRequestException("Account already exists");
        }

        AccountEntity newAccount = new AccountEntity();

        newAccount.setInitialBalance(createAccountDTO.initialBalance());

        AccountEntity savedAccount = accountRepository.save(newAccount);

        return this.mapToResponseDTO(savedAccount);
    }

    @Transactional
    public AccountResponseDTO updateAccount(UpdateAccountDTO updateAccountDTO) {
        AccountEntity account = getAccountOrThrow();

        account.setInitialBalance(updateAccountDTO.initialBalance());

        AccountEntity updatedAccount = accountRepository.save(account);

        return this.mapToResponseDTO(updatedAccount);
    }

    // Mapper method
    private AccountResponseDTO mapToResponseDTO(AccountEntity account) {
        return new AccountResponseDTO(
                account.getId(),
                account.getInitialBalance(),
                account.getCreatedAt(),
                account.getUpdatedAt());
    }

    // Helper method
    private AccountEntity getAccountOrThrow() {
        return accountRepository
                .findFirstByOrderByCreatedAt()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account was not initialized or found"));
    }
}
