package com.mauro.cashiogram.transaction;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mauro.cashiogram.transaction.dto.CreateTransactionDTO;
import com.mauro.cashiogram.transaction.dto.CreateTransactionSplitDTO;
import com.mauro.cashiogram.transaction.dto.TransactionResponseDTO;
import com.mauro.cashiogram.transaction.dto.TransactionSplitResponseDTO;
import com.mauro.cashiogram.transaction.dto.UpdateTransactionDTO;
import com.mauro.cashiogram.transaction.dto.UpdateTransactionSplitDTO;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionSplitService transactionSplitService;

    public TransactionController(
            TransactionService transactionService,
            TransactionSplitService transactionSplitService) {

        this.transactionService = transactionService;
        this.transactionSplitService = transactionSplitService;
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDTO>> getTransactions() {
        return ResponseEntity.ok(
                transactionService.getTransactions());
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> getTransactionById(
            @PathVariable("transactionId") UUID transactionId) {

        return ResponseEntity.ok(
                transactionService.getTransactionById(transactionId));
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(
            @Valid @RequestBody CreateTransactionDTO createTransactionDTO) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        transactionService.createTransaction(
                                createTransactionDTO));
    }

    @PatchMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable("transactionId") UUID transactionId,
            @Valid @RequestBody UpdateTransactionDTO updateTransactionDTO) {

        return ResponseEntity.ok(
                transactionService.updateTransaction(
                        transactionId,
                        updateTransactionDTO));
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable("transactionId") UUID transactionId) {

        transactionService.deleteTransaction(transactionId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{transactionId}/splits")
    public ResponseEntity<TransactionSplitResponseDTO> createSplit(
            @PathVariable("transactionId") UUID transactionId,
            @Valid @RequestBody CreateTransactionSplitDTO createSplitDTO) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        transactionSplitService.createSplit(
                                transactionId,
                                createSplitDTO));
    }

    @PatchMapping("/{transactionId}/splits/{splitId}")
    public ResponseEntity<TransactionSplitResponseDTO> updateSplit(
            @PathVariable("transactionId") UUID transactionId,
            @PathVariable("splitId") UUID splitId,
            @Valid @RequestBody UpdateTransactionSplitDTO updateSplitDTO) {

        return ResponseEntity.ok(
                transactionSplitService.updateSplit(
                        transactionId,
                        splitId,
                        updateSplitDTO));
    }

    @DeleteMapping("/{transactionId}/splits/{splitId}")
    public ResponseEntity<Void> deleteSplit(
            @PathVariable("transactionId") UUID transactionId,
            @PathVariable("splitId") UUID splitId) {

        transactionSplitService.deleteSplit(
                transactionId,
                splitId);

        return ResponseEntity.noContent().build();
    }
}