package com.mauro.cashiogram.transaction.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.mauro.cashiogram.subcategory.entity.SubcategoryEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "transaction_splits", uniqueConstraints = @UniqueConstraint(columnNames = { "transaction_id",
        "subcategory_id" }))
public class TransactionSplitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = false)
    private TransactionEntity transaction;

    @ManyToOne
    @JoinColumn(name = "subcategory_id", nullable = false)
    private SubcategoryEntity subcategory;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
}
