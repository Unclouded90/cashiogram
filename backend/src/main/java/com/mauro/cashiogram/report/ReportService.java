package com.mauro.cashiogram.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public PeriodTotalDTO getPeriodTotal(
            LocalDate from,
            LocalDate to) {

        validatePeriod(from, to);

        BigDecimal totalIncome = reportRepository.sumAmountByTypeAndPeriod(
                TransactionType.INCOME,
                from,
                to);

        BigDecimal totalExpenses = reportRepository.sumAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to);

        BigDecimal netTotal = totalIncome.subtract(totalExpenses);

        return new PeriodTotalDTO(
                from,
                to,
                totalIncome,
                totalExpenses,
                netTotal);
    }

    public List<CategoryTotalDTO> getCategoryTotals(
            TransactionType transactionType,
            LocalDate from,
            LocalDate to) {

        validatePeriod(from, to);

        return reportRepository.getTotalsByCategory(
                transactionType,
                from,
                to);
    }

    public List<SubcategoryTotalDTO> getSubcategoryTotals(
            TransactionType transactionType,
            LocalDate from,
            LocalDate to) {

        validatePeriod(from, to);

        return reportRepository.getTotalsBySubcategory(
                transactionType,
                from,
                to);
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
}