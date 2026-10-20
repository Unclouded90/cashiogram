package com.mauro.cashiogram.balance.repository;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.mauro.cashiogram.transaction.entity.TransactionEntity;
import com.mauro.cashiogram.transaction.entity.TransactionType;

public interface BalanceRepository
        extends Repository<TransactionEntity, UUID> {

    @Query("""
                SELECT COALESCE(SUM(t.amount), 0)
                FROM TransactionEntity t
                WHERE t.transactionType = :transactionType
            """)
    BigDecimal sumAmountByTransactionType(
            @Param("transactionType") TransactionType transactionType);
}