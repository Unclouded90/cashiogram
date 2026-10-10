package com.mauro.cashiogram.account.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mauro.cashiogram.account.entity.AccountEntity;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    // FIND
    Optional<AccountEntity> findFirstByOrderByCreatedAt();
}
