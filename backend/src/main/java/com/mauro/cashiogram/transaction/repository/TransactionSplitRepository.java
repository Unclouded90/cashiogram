package com.mauro.cashiogram.transaction.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mauro.cashiogram.transaction.entity.TransactionSplitEntity;

public interface TransactionSplitRepository
        extends JpaRepository<TransactionSplitEntity, UUID> {

    List<TransactionSplitEntity> findByTransactionId(UUID transactionId);

    Optional<TransactionSplitEntity> findByIdAndTransactionId(
            UUID id,
            UUID transactionId);

    boolean existsByTransactionIdAndSubcategoryId(
            UUID transactionId,
            UUID subcategoryId);

    boolean existsByTransactionIdAndSubcategoryIdAndIdNot(
            UUID transactionId,
            UUID subcategoryId,
            UUID splitId);

    boolean existsBySubcategoryId(UUID subcategoryId);

    void deleteByTransactionId(UUID transactionId);
}