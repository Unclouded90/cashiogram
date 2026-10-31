package com.mauro.cashiogram.account.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.mauro.cashiogram.account.entity.AccountEntity;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldFindFirstAccountByCreatedAt() {
        AccountEntity firstAccount = createAccount(
                new BigDecimal("1000.00"));

        createAccount(
                new BigDecimal("2000.00"));

        Optional<AccountEntity> result = accountRepository.findFirstByOrderByCreatedAt();

        assertTrue(result.isPresent());

        assertEquals(
                firstAccount.getId(),
                result.get().getId());

        assertEquals(
                new BigDecimal("1000.00"),
                result.get().getInitialBalance());
    }

    @Test
    void shouldUpdateUpdatedAtWhenAccountIsUpdated() {
        AccountEntity account = createAccount(
                new BigDecimal("1000.00"));

        Instant updatedAtBefore = account.getUpdatedAt();

        assertNotNull(updatedAtBefore);

        account.setInitialBalance(new BigDecimal("1500.00"));

        AccountEntity updatedAccount = accountRepository.saveAndFlush(account);

        Instant updatedAtAfter = updatedAccount.getUpdatedAt();

        assertNotNull(updatedAtAfter);
        assertTrue(updatedAtAfter.isAfter(updatedAtBefore));

        assertEquals(
                new BigDecimal("1500.00"),
                updatedAccount.getInitialBalance());
    }

    // Helper methods

    private AccountEntity createAccount(BigDecimal initialBalance) {
        AccountEntity account = new AccountEntity();

        account.setInitialBalance(initialBalance);

        return accountRepository.saveAndFlush(account);
    }
}