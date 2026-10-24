package com.mauro.cashiogram.account.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
                new BigDecimal("1000.00"),
                Instant.parse("2026-10-01T10:00:00Z"));

        createAccount(
                new BigDecimal("2000.00"),
                Instant.parse("2026-10-02T10:00:00Z"));

        Optional<AccountEntity> result = accountRepository.findFirstByOrderByCreatedAt();

        assertTrue(result.isPresent());

        assertEquals(
                firstAccount.getId(),
                result.get().getId());

        assertEquals(
                new BigDecimal("1000.00"),
                result.get().getInitialBalance());
    }

    // Helper methods

    private AccountEntity createAccount(
            BigDecimal initialBalance,
            Instant createdAt) {

        AccountEntity account = new AccountEntity();

        account.setInitialBalance(initialBalance);
        account.setCreatedAt(createdAt);
        account.setUpdatedAt(createdAt);

        return accountRepository.save(account);
    }
}