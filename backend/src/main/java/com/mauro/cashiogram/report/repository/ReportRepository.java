package com.mauro.cashiogram.report.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.mauro.cashiogram.report.dto.CategoryTotalDTO;
import com.mauro.cashiogram.report.dto.SubcategoryTotalDTO;
import com.mauro.cashiogram.transaction.entity.TransactionEntity;
import com.mauro.cashiogram.transaction.entity.TransactionType;

public interface ReportRepository
        extends Repository<TransactionEntity, UUID> {

    @Query("""
                SELECT COALESCE(SUM(t.amount), 0)
                FROM TransactionEntity t
                WHERE t.transactionType = :transactionType
                  AND t.date BETWEEN :from AND :to
            """)
    BigDecimal sumAmountByTypeAndPeriod(
            @Param("transactionType") TransactionType transactionType,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
                SELECT new com.mauro.cashiogram.report.dto.CategoryTotalDTO(
                    c.id,
                    c.name,
                    SUM(t.amount)
                )
                FROM TransactionEntity t
                JOIN t.category c
                WHERE t.transactionType = :transactionType
                  AND t.date BETWEEN :from AND :to
                GROUP BY c.id, c.name
                ORDER BY SUM(t.amount) DESC
            """)
    List<CategoryTotalDTO> getTotalsByCategory(
            @Param("transactionType") TransactionType transactionType,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
                SELECT new com.mauro.cashiogram.report.dto.SubcategoryTotalDTO(
                    s.subcategory.id,
                    s.subcategory.name,
                    SUM(s.amount)
                )
                FROM TransactionSplitEntity s
                WHERE s.transaction.transactionType = :transactionType
                  AND s.transaction.date BETWEEN :from AND :to
                GROUP BY s.subcategory.id, s.subcategory.name
                ORDER BY SUM(s.amount) DESC
            """)
    List<SubcategoryTotalDTO> getTotalsBySubcategory(
            @Param("transactionType") TransactionType transactionType,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}