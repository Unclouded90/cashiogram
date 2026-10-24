package com.mauro.cashiogram.report;

import java.time.LocalDate;
import java.util.List;

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

            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(
                reportService.getPeriodTotal(from, to));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryTotalDTO>> getCategoryTotals(
            @RequestParam("type") TransactionType transactionType,

            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(
                reportService.getCategoryTotals(
                        transactionType,
                        from,
                        to));
    }

    @GetMapping("/subcategories")
    public ResponseEntity<List<SubcategoryTotalDTO>> getSubcategoryTotals(
            @RequestParam("type") TransactionType transactionType,

            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,

            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(
                reportService.getSubcategoryTotals(
                        transactionType,
                        from,
                        to));
    }
}