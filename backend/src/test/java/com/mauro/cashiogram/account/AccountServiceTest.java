package com.mauro.cashiogram.account;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mauro.cashiogram.account.dto.AccountResponseDTO;
import com.mauro.cashiogram.account.dto.CreateAccountDTO;
import com.mauro.cashiogram.account.dto.UpdateAccountDTO;
import com.mauro.cashiogram.account.entity.AccountEntity;
import com.mauro.cashiogram.account.repository.AccountRepository;
import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private AccountEntity account;

    @BeforeEach
    void setup() {
        account = new AccountEntity();
        account.setId(UUID.randomUUID());
        account.setInitialBalance(BigDecimal.valueOf(2000));
    }

    @Test
    void itShouldGetAccount() {
        when(accountRepository.findFirstByOrderByCreatedAt())
                .thenReturn(Optional.of(account));

        AccountResponseDTO result = accountService.getAccount();

        assertEquals(account.getId(), result.id());
        assertEquals(account.getInitialBalance(), result.initialBalance());

        verify(accountRepository).findFirstByOrderByCreatedAt();
    }

    @Test
    void itShouldNotGetAccount() {
        when(accountRepository.findFirstByOrderByCreatedAt())
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.getAccount());

        verify(accountRepository).findFirstByOrderByCreatedAt();
    }

    @Test
    void itShouldCreateAccount() {
        CreateAccountDTO request = new CreateAccountDTO(BigDecimal.valueOf(3000));

        AccountEntity savedAccount = new AccountEntity();
        savedAccount.setId(UUID.randomUUID());
        savedAccount.setInitialBalance(request.initialBalance());

        when(accountRepository.findFirstByOrderByCreatedAt())
                .thenReturn(Optional.empty());

        when(accountRepository.save(any(AccountEntity.class)))
                .thenReturn(savedAccount);

        AccountResponseDTO result = accountService.createAccount(request);

        assertEquals(savedAccount.getId(), result.id());
        assertEquals(savedAccount.getInitialBalance(), result.initialBalance());

        verify(accountRepository).findFirstByOrderByCreatedAt();
        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    void itShouldFailToCreateAccountIfAccountAlreadyExists() {
        CreateAccountDTO request = new CreateAccountDTO(BigDecimal.valueOf(3000));

        when(accountRepository.findFirstByOrderByCreatedAt())
                .thenReturn(Optional.of(account));

        assertThrows(
                BadRequestException.class,
                () -> accountService.createAccount(request));

        verify(accountRepository).findFirstByOrderByCreatedAt();
        verify(accountRepository, never())
                .save(any(AccountEntity.class));
    }

    @Test
    void itShouldUpdateAccount() {
        UpdateAccountDTO request = new UpdateAccountDTO(BigDecimal.valueOf(4500));

        when(accountRepository.findFirstByOrderByCreatedAt())
                .thenReturn(Optional.of(account));

        when(accountRepository.save(account))
                .thenReturn(account);

        AccountResponseDTO result = accountService.updateAccount(request);

        assertEquals(request.initialBalance(), result.initialBalance());

        verify(accountRepository).findFirstByOrderByCreatedAt();
        verify(accountRepository).save(account);
    }

    @Test
    void itShouldFailToUpdateAccountIfNotFound() {
        UpdateAccountDTO request = new UpdateAccountDTO(BigDecimal.valueOf(4500));

        when(accountRepository.findFirstByOrderByCreatedAt())
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.updateAccount(request));

        verify(accountRepository).findFirstByOrderByCreatedAt();
        verify(accountRepository, never())
                .save(any(AccountEntity.class));
    }
}