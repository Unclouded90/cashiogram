package com.mauro.cashiogram.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.report.dto.CategoryTotalDTO;
import com.mauro.cashiogram.report.dto.PeriodTotalDTO;
import com.mauro.cashiogram.report.dto.SubcategoryTotalDTO;
import com.mauro.cashiogram.report.repository.ReportRepository;
import com.mauro.cashiogram.transaction.entity.TransactionType;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;

    private static final List<UUID> NO_CATEGORY_FILTER = List.of(new UUID(0L, 0L));

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public PeriodTotalDTO getPeriodTotal(
            LocalDate from,
            LocalDate to,
            List<UUID> categoryIds,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String subcategoryName,
            BigDecimal minSplitAmount,
            BigDecimal maxSplitAmount) {

        validatePeriod(from, to);
        validateAmountRange(minAmount, maxAmount);
        validateSplitAmountRange(minSplitAmount, maxSplitAmount);

        String name = normalizeSubcategoryName(subcategoryName);

        BigDecimal totalIncome = getTotalByType(
                TransactionType.INCOME,
                from,
                to,
                categoryIds,
                minAmount,
                maxAmount,
                name,
                minSplitAmount,
                maxSplitAmount);

        BigDecimal totalExpenses = getTotalByType(
                TransactionType.EXPENSE,
                from,
                to,
                categoryIds,
                minAmount,
                maxAmount,
                name,
                minSplitAmount,
                maxSplitAmount);

        return new PeriodTotalDTO(
                from,
                to,
                totalIncome,
                totalExpenses,
                totalIncome.subtract(totalExpenses));
    }

    public List<CategoryTotalDTO> getCategoryTotals(
            TransactionType transactionType,
            LocalDate from,
            LocalDate to,
            List<UUID> categoryIds,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String subcategoryName,
            BigDecimal minSplitAmount,
            BigDecimal maxSplitAmount) {

        validatePeriod(from, to);
        validateAmountRange(minAmount, maxAmount);
        validateSplitAmountRange(minSplitAmount, maxSplitAmount);

        String name = normalizeSubcategoryName(subcategoryName);

        boolean filterByCategory = categoryIds != null && !categoryIds.isEmpty();

        boolean filterMinAmount = minAmount != null;
        boolean filterMaxAmount = maxAmount != null;

        List<UUID> ids = effectiveCategoryIds(categoryIds);

        BigDecimal min = valueOrZero(minAmount);
        BigDecimal max = valueOrZero(maxAmount);

        if (!hasSplitFilters(name, minSplitAmount, maxSplitAmount)) {
            return reportRepository.getTotalsByCategory(
                    transactionType,
                    from,
                    to,
                    filterByCategory,
                    ids,
                    filterMinAmount,
                    min,
                    filterMaxAmount,
                    max);
        }

        return reportRepository.getFilteredSplitTotalsByCategory(
                transactionType,
                from,
                to,
                name,
                filterByCategory,
                ids,
                filterMinAmount,
                min,
                filterMaxAmount,
                max,
                minSplitAmount != null,
                valueOrZero(minSplitAmount),
                maxSplitAmount != null,
                valueOrZero(maxSplitAmount));
    }

    public List<SubcategoryTotalDTO> getSubcategoryTotals(
            TransactionType transactionType,
            LocalDate from,
            LocalDate to,
            String subcategoryName,
            List<UUID> categoryIds,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            BigDecimal minSplitAmount,
            BigDecimal maxSplitAmount) {

        validatePeriod(from, to);
        validateAmountRange(minAmount, maxAmount);
        validateSplitAmountRange(minSplitAmount, maxSplitAmount);

        String name = normalizeSubcategoryName(subcategoryName);

        boolean filterByCategory = categoryIds != null && !categoryIds.isEmpty();

        return reportRepository.getTotalsBySubcategory(
                transactionType,
                from,
                to,
                name,
                filterByCategory,
                effectiveCategoryIds(categoryIds),
                minAmount != null,
                valueOrZero(minAmount),
                maxAmount != null,
                valueOrZero(maxAmount),
                minSplitAmount != null,
                valueOrZero(minSplitAmount),
                maxSplitAmount != null,
                valueOrZero(maxSplitAmount));
    }

    // Helper methods

    private void validatePeriod(
            LocalDate from,
            LocalDate to) {

        if (from == null || to == null) {
            throw new BadRequestException(
                    "Both from and to dates are required");
        }

        if (from.isAfter(to)) {
            throw new BadRequestException(
                    "From date cannot be after to date");
        }
    }

    private List<UUID> effectiveCategoryIds(List<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return NO_CATEGORY_FILTER;
        }

        return categoryIds;
    }

    private void validateAmountRange(
            BigDecimal minAmount,
            BigDecimal maxAmount) {

        if (minAmount != null && minAmount.signum() < 0) {
            throw new BadRequestException(
                    "Minimum amount cannot be negative");
        }

        if (maxAmount != null && maxAmount.signum() < 0) {
            throw new BadRequestException(
                    "Maximum amount cannot be negative");
        }

        if (minAmount != null
                && maxAmount != null
                && minAmount.compareTo(maxAmount) > 0) {
            throw new BadRequestException(
                    "Minimum amount cannot exceed maximum amount");
        }
    }

    private BigDecimal getTotalByType(
            TransactionType transactionType,
            LocalDate from,
            LocalDate to,
            List<UUID> categoryIds,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String subcategoryName,
            BigDecimal minSplitAmount,
            BigDecimal maxSplitAmount) {

        boolean filterByCategory = categoryIds != null && !categoryIds.isEmpty();

        boolean filterMinAmount = minAmount != null;
        boolean filterMaxAmount = maxAmount != null;

        List<UUID> ids = effectiveCategoryIds(categoryIds);

        BigDecimal min = valueOrZero(minAmount);
        BigDecimal max = valueOrZero(maxAmount);

        if (!hasSplitFilters(
                subcategoryName,
                minSplitAmount,
                maxSplitAmount)) {

            return reportRepository.sumAmountByTypeAndPeriod(
                    transactionType,
                    from,
                    to,
                    filterByCategory,
                    ids,
                    filterMinAmount,
                    min,
                    filterMaxAmount,
                    max);
        }

        return reportRepository.sumFilteredSplitAmountByTypeAndPeriod(
                transactionType,
                from,
                to,
                subcategoryName,
                filterByCategory,
                ids,
                filterMinAmount,
                min,
                filterMaxAmount,
                max,
                minSplitAmount != null,
                valueOrZero(minSplitAmount),
                maxSplitAmount != null,
                valueOrZero(maxSplitAmount));
    }

    private boolean hasSplitFilters(
            String subcategoryName,
            BigDecimal minSplitAmount,
            BigDecimal maxSplitAmount) {

        return !subcategoryName.isEmpty()
                || minSplitAmount != null
                || maxSplitAmount != null;
    }

    private String normalizeSubcategoryName(String subcategoryName) {
        return subcategoryName == null
                ? ""
                : subcategoryName.trim();
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private void validateSplitAmountRange(
            BigDecimal minSplitAmount,
            BigDecimal maxSplitAmount) {

        if (minSplitAmount != null
                && minSplitAmount.signum() < 0) {
            throw new BadRequestException(
                    "Minimum split amount cannot be negative");
        }

        if (maxSplitAmount != null
                && maxSplitAmount.signum() < 0) {
            throw new BadRequestException(
                    "Maximum split amount cannot be negative");
        }

        if (minSplitAmount != null
                && maxSplitAmount != null
                && minSplitAmount.compareTo(maxSplitAmount) > 0) {
            throw new BadRequestException(
                    "Minimum split amount cannot exceed maximum split amount");
        }
    }
}