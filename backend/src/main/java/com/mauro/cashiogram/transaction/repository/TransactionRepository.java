package com.mauro.cashiogram.transaction.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mauro.cashiogram.transaction.entity.TransactionEntity;

public interface TransactionRepository
        extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findAllByOrderByDateDesc();
}