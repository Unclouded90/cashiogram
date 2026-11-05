package com.mauro.cashiogram.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mauro.cashiogram.report.dto.CategoryTotalDTO;
import com.mauro.cashiogram.report.dto.PeriodTotalDTO;
import com.mauro.cashiogram.report.dto.SubcategoryTotalDTO;
import com.mauro.cashiogram.transaction.entity.TransactionType;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/period")
    public ResponseEntity<PeriodTotalDTO> getPeriodTotal(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @RequestParam(name = "categoryIds", required = false) List<UUID> categoryIds,

            @RequestParam(name = "minAmount", required = false) BigDecimal minAmount,

            @RequestParam(name = "maxAmount", required = false) BigDecimal maxAmount,

            @RequestParam(name = "subcategoryName", required = false) String subcategoryName,

            @RequestParam(name = "minSplitAmount", required = false) BigDecimal minSplitAmount,

            @RequestParam(name = "maxSplitAmount", required = false) BigDecimal maxSplitAmount) {

        return ResponseEntity.ok(
                reportService.getPeriodTotal(
                        from,
                        to,
                        categoryIds,
                        minAmount,
                        maxAmount,
                        subcategoryName,
                        minSplitAmount,
                        maxSplitAmount));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryTotalDTO>> getCategoryTotals(
            @RequestParam("type") TransactionType transactionType,

            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @RequestParam(name = "categoryIds", required = false) List<UUID> categoryIds,

            @RequestParam(name = "minAmount", required = false) BigDecimal minAmount,

            @RequestParam(name = "maxAmount", required = false) BigDecimal maxAmount,

            @RequestParam(name = "subcategoryName", required = false) String subcategoryName,

            @RequestParam(name = "minSplitAmount", required = false) BigDecimal minSplitAmount,

            @RequestParam(name = "maxSplitAmount", required = false) BigDecimal maxSplitAmount) {

        return ResponseEntity.ok(
                reportService.getCategoryTotals(
                        transactionType,
                        from,
                        to,
                        categoryIds,
                        minAmount,
                        maxAmount,
                        subcategoryName,
                        minSplitAmount,
                        maxSplitAmount));
    }

    @GetMapping("/subcategories")
    public ResponseEntity<List<SubcategoryTotalDTO>> getSubcategoryTotals(
            @RequestParam("type") TransactionType transactionType,

            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,

            @RequestParam(name = "subcategoryName", required = false) String subcategoryName,

            @RequestParam(name = "categoryIds", required = false) List<UUID> categoryIds,

            @RequestParam(name = "minAmount", required = false) BigDecimal minAmount,

            @RequestParam(name = "maxAmount", required = false) BigDecimal maxAmount,

            @RequestParam(name = "minSplitAmount", required = false) BigDecimal minSplitAmount,

            @RequestParam(name = "maxSplitAmount", required = false) BigDecimal maxSplitAmount) {

        return ResponseEntity.ok(
                reportService.getSubcategoryTotals(
                        transactionType,
                        from,
                        to,
                        subcategoryName,
                        categoryIds,
                        minAmount,
                        maxAmount,
                        minSplitAmount,
                        maxSplitAmount));
    }
}