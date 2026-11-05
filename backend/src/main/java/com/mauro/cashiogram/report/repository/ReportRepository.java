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
              AND (
                  :filterByCategory = false
                  OR t.category.id IN :categoryIds
              )
              AND (
                  :filterMinAmount = false
                  OR t.amount >= :minAmount
              )
              AND (
                  :filterMaxAmount = false
                  OR t.amount <= :maxAmount
              )
            """)
    BigDecimal sumAmountByTypeAndPeriod(
            @Param("transactionType") TransactionType transactionType,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("filterByCategory") boolean filterByCategory,
            @Param("categoryIds") List<UUID> categoryIds,
            @Param("filterMinAmount") boolean filterMinAmount,
            @Param("minAmount") BigDecimal minAmount,
            @Param("filterMaxAmount") boolean filterMaxAmount,
            @Param("maxAmount") BigDecimal maxAmount);

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
              AND (
                  :filterByCategory = false
                  OR c.id IN :categoryIds
              )
              AND (
                  :filterMinAmount = false
                  OR t.amount >= :minAmount
              )
              AND (
                  :filterMaxAmount = false
                  OR t.amount <= :maxAmount
              )
            GROUP BY c.id, c.name
            ORDER BY SUM(t.amount) DESC
            """)
    List<CategoryTotalDTO> getTotalsByCategory(
            @Param("transactionType") TransactionType transactionType,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("filterByCategory") boolean filterByCategory,
            @Param("categoryIds") List<UUID> categoryIds,
            @Param("filterMinAmount") boolean filterMinAmount,
            @Param("minAmount") BigDecimal minAmount,
            @Param("filterMaxAmount") boolean filterMaxAmount,
            @Param("maxAmount") BigDecimal maxAmount);

    @Query("""
            SELECT new com.mauro.cashiogram.report.dto.SubcategoryTotalDTO(
                sc.id,
                sc.name,
                c.id,
                c.name,
                SUM(s.amount)
            )
            FROM TransactionSplitEntity s
            JOIN s.transaction t
            JOIN s.subcategory sc
            JOIN t.category c
            WHERE t.transactionType = :transactionType
              AND t.date BETWEEN :from AND :to
              AND (
                  :subcategoryName = ''
                  OR LOWER(sc.name) LIKE CONCAT('%', LOWER(:subcategoryName), '%')
              )
              AND (
                  :filterByCategory = false
                  OR c.id IN :categoryIds
              )
              AND (
                  :filterMinAmount = false
                  OR t.amount >= :minAmount
              )
              AND (
                  :filterMaxAmount = false
                  OR t.amount <= :maxAmount
              )
              AND (
                  :filterMinSplitAmount = false
                  OR s.amount >= :minSplitAmount
              )
              AND (
                  :filterMaxSplitAmount = false
                  OR s.amount <= :maxSplitAmount
              )
            GROUP BY sc.id, sc.name, c.id, c.name
            ORDER BY SUM(s.amount) DESC
            """)
    List<SubcategoryTotalDTO> getTotalsBySubcategory(
            @Param("transactionType") TransactionType transactionType,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("subcategoryName") String subcategoryName,
            @Param("filterByCategory") boolean filterByCategory,
            @Param("categoryIds") List<UUID> categoryIds,
            @Param("filterMinAmount") boolean filterMinAmount,
            @Param("minAmount") BigDecimal minAmount,
            @Param("filterMaxAmount") boolean filterMaxAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("filterMinSplitAmount") boolean filterMinSplitAmount,
            @Param("minSplitAmount") BigDecimal minSplitAmount,
            @Param("filterMaxSplitAmount") boolean filterMaxSplitAmount,
            @Param("maxSplitAmount") BigDecimal maxSplitAmount);

    @Query("""
            SELECT COALESCE(SUM(s.amount), 0)
            FROM TransactionSplitEntity s
            JOIN s.transaction t
            JOIN s.subcategory sc
            WHERE t.transactionType = :transactionType
              AND t.date BETWEEN :from AND :to
              AND (
                  :subcategoryName = ''
                  OR LOWER(sc.name) LIKE CONCAT('%', LOWER(:subcategoryName), '%')
              )
              AND (
                  :filterByCategory = false
                  OR t.category.id IN :categoryIds
              )
              AND (
                  :filterMinAmount = false
                  OR t.amount >= :minAmount
              )
              AND (
                  :filterMaxAmount = false
                  OR t.amount <= :maxAmount
              )
              AND (
                  :filterMinSplitAmount = false
                  OR s.amount >= :minSplitAmount
              )
              AND (
                  :filterMaxSplitAmount = false
                  OR s.amount <= :maxSplitAmount
              )
            """)
    BigDecimal sumFilteredSplitAmountByTypeAndPeriod(
            @Param("transactionType") TransactionType transactionType,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("subcategoryName") String subcategoryName,
            @Param("filterByCategory") boolean filterByCategory,
            @Param("categoryIds") List<UUID> categoryIds,
            @Param("filterMinAmount") boolean filterMinAmount,
            @Param("minAmount") BigDecimal minAmount,
            @Param("filterMaxAmount") boolean filterMaxAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("filterMinSplitAmount") boolean filterMinSplitAmount,
            @Param("minSplitAmount") BigDecimal minSplitAmount,
            @Param("filterMaxSplitAmount") boolean filterMaxSplitAmount,
            @Param("maxSplitAmount") BigDecimal maxSplitAmount);

    @Query("""
            SELECT new com.mauro.cashiogram.report.dto.CategoryTotalDTO(
                c.id,
                c.name,
                SUM(s.amount)
            )
            FROM TransactionSplitEntity s
            JOIN s.transaction t
            JOIN t.category c
            JOIN s.subcategory sc
            WHERE t.transactionType = :transactionType
              AND t.date BETWEEN :from AND :to
              AND (
                  :subcategoryName = ''
                  OR LOWER(sc.name) LIKE CONCAT('%', LOWER(:subcategoryName), '%')
              )
              AND (
                  :filterByCategory = false
                  OR c.id IN :categoryIds
              )
              AND (
                  :filterMinAmount = false
                  OR t.amount >= :minAmount
              )
              AND (
                  :filterMaxAmount = false
                  OR t.amount <= :maxAmount
              )
              AND (
                  :filterMinSplitAmount = false
                  OR s.amount >= :minSplitAmount
              )
              AND (
                  :filterMaxSplitAmount = false
                  OR s.amount <= :maxSplitAmount
              )
            GROUP BY c.id, c.name
            ORDER BY SUM(s.amount) DESC
            """)
    List<CategoryTotalDTO> getFilteredSplitTotalsByCategory(
            @Param("transactionType") TransactionType transactionType,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("subcategoryName") String subcategoryName,
            @Param("filterByCategory") boolean filterByCategory,
            @Param("categoryIds") List<UUID> categoryIds,
            @Param("filterMinAmount") boolean filterMinAmount,
            @Param("minAmount") BigDecimal minAmount,
            @Param("filterMaxAmount") boolean filterMaxAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("filterMinSplitAmount") boolean filterMinSplitAmount,
            @Param("minSplitAmount") BigDecimal minSplitAmount,
            @Param("filterMaxSplitAmount") boolean filterMaxSplitAmount,
            @Param("maxSplitAmount") BigDecimal maxSplitAmount);
}