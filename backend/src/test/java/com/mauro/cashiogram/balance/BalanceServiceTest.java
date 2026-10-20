package com.mauro.cashiogram.balance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mauro.cashiogram.account.entity.AccountEntity;
import com.mauro.cashiogram.account.repository.AccountRepository;
import com.mauro.cashiogram.balance.dto.BalanceResponseDTO;
import com.mauro.cashiogram.balance.repository.BalanceRepository;
import com.mauro.cashiogram.exception.ResourceNotFoundException;
import com.mauro.cashiogram.transaction.entity.TransactionType;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BalanceRepository balanceRepository;

    private BalanceService balanceService;

    @BeforeEach
    void setUp() {
        balanceService = new BalanceService(
                accountRepository,
                balanceRepository);
    }

    @Test
    void shouldCalculateBalance() {
        AccountEntity account = createAccount(
                new BigDecimal("1000.00"));

        when(accountRepository.findFirstByOrderByCreatedAt())
                .thenReturn(Optional.of(account));

        when(balanceRepository.sumAmountByTransactionType(
                TransactionType.INCOME))
                .thenReturn(new BigDecimal("2200.00"));

        when(balanceRepository.sumAmountByTransactionType(
                TransactionType.EXPENSE))
                .thenReturn(new BigDecimal("850.00"));

        BalanceResponseDTO result = balanceService.getBalance();

        assertEquals(
                new BigDecimal("1000.00"),
                result.initialBalance());

        assertEquals(
                new BigDecimal("2200.00"),
                result.totalIncome());

        assertEquals(
                new BigDecimal("850.00"),
                result.totalExpenses());

        assertEquals(
                new BigDecimal("2350.00"),
                result.currentBalance());
    }

    @Test
    void shouldReturnInitialBalanceWhenThereAreNoTransactions() {
        AccountEntity account = createAccount(
                new BigDecimal("1000.00"));

        when(accountRepository.findFirstByOrderByCreatedAt())
                .thenReturn(Optional.of(account));

        when(balanceRepository.sumAmountByTransactionType(
                TransactionType.INCOME))
                .thenReturn(BigDecimal.ZERO);

        when(balanceRepository.sumAmountByTransactionType(
                TransactionType.EXPENSE))
                .thenReturn(BigDecimal.ZERO);

        BalanceResponseDTO result = balanceService.getBalance();

        assertEquals(
                new BigDecimal("1000.00"),
                result.currentBalance());

        assertEquals(
                BigDecimal.ZERO,
                result.totalIncome());

        assertEquals(
                BigDecimal.ZERO,
                result.totalExpenses());
    }

    @Test
    void shouldCalculateNegativeBalance() {
        AccountEntity account = createAccount(
                new BigDecimal("100.00"));

        when(accountRepository.findFirstByOrderByCreatedAt())
                .thenReturn(Optional.of(account));

        when(balanceRepository.sumAmountByTransactionType(
                TransactionType.INCOME))
                .thenReturn(BigDecimal.ZERO);

        when(balanceRepository.sumAmountByTransactionType(
                TransactionType.EXPENSE))
                .thenReturn(new BigDecimal("250.00"));

        BalanceResponseDTO result = balanceService.getBalance();

        assertEquals(
                new BigDecimal("-150.00"),
                result.currentBalance());
    }

    @Test
    void shouldFailWhenAccountIsNotInitialized() {
        when(accountRepository.findFirstByOrderByCreatedAt())
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> balanceService.getBalance());

        verify(balanceRepository, never())
                .sumAmountByTransactionType(
                        TransactionType.INCOME);

        verify(balanceRepository, never())
                .sumAmountByTransactionType(
                        TransactionType.EXPENSE);
    }

    // Helper methods

    private AccountEntity createAccount(
            BigDecimal initialBalance) {

        AccountEntity account = new AccountEntity();
        account.setInitialBalance(initialBalance);

        return account;
    }
}