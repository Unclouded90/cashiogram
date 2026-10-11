package com.mauro.cashiogram.transaction;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.exception.ResourceNotFoundException;
import com.mauro.cashiogram.subcategory.entity.SubcategoryEntity;
import com.mauro.cashiogram.subcategory.repository.SubcategoryRepository;
import com.mauro.cashiogram.transaction.dto.CreateTransactionSplitDTO;
import com.mauro.cashiogram.transaction.dto.TransactionSplitResponseDTO;
import com.mauro.cashiogram.transaction.dto.UpdateTransactionSplitDTO;
import com.mauro.cashiogram.transaction.entity.TransactionEntity;
import com.mauro.cashiogram.transaction.entity.TransactionSplitEntity;
import com.mauro.cashiogram.transaction.repository.TransactionRepository;
import com.mauro.cashiogram.transaction.repository.TransactionSplitRepository;

@Service
@Transactional(readOnly = true)
public class TransactionSplitService {

    private final TransactionRepository transactionRepository;
    private final TransactionSplitRepository transactionSplitRepository;
    private final SubcategoryRepository subcategoryRepository;

    public TransactionSplitService(
            TransactionRepository transactionRepository,
            TransactionSplitRepository transactionSplitRepository,
            SubcategoryRepository subcategoryRepository) {

        this.transactionRepository = transactionRepository;
        this.transactionSplitRepository = transactionSplitRepository;
        this.subcategoryRepository = subcategoryRepository;
    }

    @Transactional
    public TransactionSplitResponseDTO createSplit(
            UUID transactionId,
            CreateTransactionSplitDTO createSplitDTO) {

        TransactionEntity transaction = getTransactionOrThrow(transactionId);

        SubcategoryEntity subcategory = getSubcategoryOrThrow(createSplitDTO.subcategoryId());

        validateSubcategory(
                transaction,
                subcategory);

        if (transactionSplitRepository
                .existsByTransactionIdAndSubcategoryId(
                        transactionId,
                        subcategory.getId())) {

            throw new BadRequestException(
                    "A split for this subcategory already exists");
        }

        BigDecimal existingTotal = getSplitTotal(transactionId);

        BigDecimal newTotal = existingTotal.add(createSplitDTO.amount());

        if (newTotal.compareTo(transaction.getAmount()) > 0) {
            throw new BadRequestException(
                    "The total split amount cannot exceed the transaction amount");
        }

        TransactionSplitEntity split = new TransactionSplitEntity();

        split.setTransaction(transaction);
        split.setSubcategory(subcategory);
        split.setAmount(createSplitDTO.amount());

        TransactionSplitEntity savedSplit = transactionSplitRepository.save(split);

        return mapToResponseDTO(savedSplit);
    }

    @Transactional
    public TransactionSplitResponseDTO updateSplit(
            UUID transactionId,
            UUID splitId,
            UpdateTransactionSplitDTO updateSplitDTO) {

        TransactionSplitEntity split = transactionSplitRepository
                .findByIdAndTransactionId(splitId, transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "The transaction split id "
                                + splitId
                                + " was not found"));

        TransactionEntity transaction = split.getTransaction();

        SubcategoryEntity subcategory = split.getSubcategory();

        if (updateSplitDTO.subcategoryId() != null) {
            subcategory = getSubcategoryOrThrow(
                    updateSplitDTO.subcategoryId());
        }

        validateSubcategory(transaction, subcategory);

        if (transactionSplitRepository
                .existsByTransactionIdAndSubcategoryIdAndIdNot(
                        transactionId,
                        subcategory.getId(),
                        splitId)) {

            throw new BadRequestException(
                    "A split for this subcategory already exists");
        }

        BigDecimal amount = split.getAmount();

        if (updateSplitDTO.amount() != null) {
            amount = updateSplitDTO.amount();
        }

        BigDecimal otherSplitsTotal = transactionSplitRepository
                .findByTransactionId(transactionId)
                .stream()
                .filter(existing -> !existing.getId().equals(splitId))
                .map(existing -> Objects.requireNonNull(existing.getAmount()))
                .reduce(
                        BigDecimal.ZERO,
                        (total, splitAmount) -> total.add(splitAmount));

        if (otherSplitsTotal
                .add(amount)
                .compareTo(transaction.getAmount()) > 0) {

            throw new BadRequestException(
                    "The total split amount cannot exceed the transaction amount");
        }

        split.setSubcategory(subcategory);
        split.setAmount(amount);

        TransactionSplitEntity updatedSplit = transactionSplitRepository.save(split);

        return mapToResponseDTO(updatedSplit);
    }

    @Transactional
    public void deleteSplit(
            UUID transactionId,
            UUID splitId) {

        TransactionSplitEntity split = transactionSplitRepository
                .findByIdAndTransactionId(splitId, transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "The transaction split id "
                                + splitId
                                + " was not found"));

        transactionSplitRepository.delete(split);
    }

    // Helper methods

    private TransactionEntity getTransactionOrThrow(
            UUID transactionId) {

        return transactionRepository
                .findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "The transaction id "
                                + transactionId
                                + " was not found"));
    }

    private SubcategoryEntity getSubcategoryOrThrow(
            UUID subcategoryId) {

        return subcategoryRepository
                .findById(subcategoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "The subcategory id "
                                + subcategoryId
                                + " was not found"));
    }

    private void validateSubcategory(
            TransactionEntity transaction,
            SubcategoryEntity subcategory) {

        if (!subcategory.getCategory().getId()
                .equals(transaction.getCategory().getId())) {

            throw new BadRequestException(
                    "Subcategory does not belong to the transaction category");
        }
    }

    private BigDecimal getSplitTotal(UUID transactionId) {
        return transactionSplitRepository
                .findByTransactionId(transactionId)
                .stream()
                .map(split -> Objects.requireNonNull(split.getAmount()))
                .reduce(
                        BigDecimal.ZERO,
                        (total, splitAmount) -> total.add(splitAmount));
    }

    // Mapper methods

    private TransactionSplitResponseDTO mapToResponseDTO(
            TransactionSplitEntity split) {

        return new TransactionSplitResponseDTO(
                split.getId(),
                split.getSubcategory().getId(),
                split.getAmount());
    }
}