package com.mauro.cashiogram.transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mauro.cashiogram.category.entity.CategoryEntity;
import com.mauro.cashiogram.category.repository.CategoryRepository;
import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.exception.ResourceNotFoundException;
import com.mauro.cashiogram.subcategory.entity.SubcategoryEntity;
import com.mauro.cashiogram.subcategory.repository.SubcategoryRepository;
import com.mauro.cashiogram.transaction.dto.CreateTransactionDTO;
import com.mauro.cashiogram.transaction.dto.CreateTransactionSplitDTO;
import com.mauro.cashiogram.transaction.dto.TransactionResponseDTO;
import com.mauro.cashiogram.transaction.dto.TransactionSplitResponseDTO;
import com.mauro.cashiogram.transaction.dto.UpdateTransactionDTO;
import com.mauro.cashiogram.transaction.entity.TransactionEntity;
import com.mauro.cashiogram.transaction.entity.TransactionSplitEntity;
import com.mauro.cashiogram.transaction.repository.TransactionRepository;
import com.mauro.cashiogram.transaction.repository.TransactionSplitRepository;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionSplitRepository transactionSplitRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            TransactionSplitRepository transactionSplitRepository,
            CategoryRepository categoryRepository,
            SubcategoryRepository subcategoryRepository) {

        this.transactionRepository = transactionRepository;
        this.transactionSplitRepository = transactionSplitRepository;
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
    }

    public List<TransactionResponseDTO> getTransactions() {
        return transactionRepository.findAllByOrderByDateDesc()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public TransactionResponseDTO getTransactionById(UUID transactionId) {
        TransactionEntity transaction = getTransactionOrThrow(transactionId);

        return mapToResponseDTO(transaction);
    }

    @Transactional
    public TransactionResponseDTO createTransaction(
            CreateTransactionDTO createTransactionDTO) {

        CategoryEntity category = categoryRepository
                .findById(createTransactionDTO.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "The category id "
                                + createTransactionDTO.categoryId()
                                + " was not found"));

        List<CreateTransactionSplitDTO> splits = createTransactionDTO.splits() != null
                ? createTransactionDTO.splits()
                : List.of();

        List<SubcategoryEntity> subcategories = new ArrayList<>();

        Set<UUID> usedSubcategories = new HashSet<>();

        BigDecimal splitTotal = BigDecimal.ZERO;

        for (CreateTransactionSplitDTO split : splits) {

            if (!usedSubcategories.add(split.subcategoryId())) {
                throw new BadRequestException(
                        "The same subcategory cannot be used more than once");
            }

            SubcategoryEntity subcategory = subcategoryRepository
                    .findById(split.subcategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "The subcategory id "
                                    + split.subcategoryId()
                                    + " was not found"));

            if (!subcategory.getCategory().getId().equals(category.getId())) {
                throw new BadRequestException(
                        "Subcategory does not belong to the transaction category");
            }

            subcategories.add(subcategory);

            splitTotal = splitTotal.add(split.amount());
        }

        if (splitTotal.compareTo(createTransactionDTO.amount()) > 0) {
            throw new BadRequestException(
                    "The total split amount cannot exceed the transaction amount");
        }

        TransactionEntity transaction = new TransactionEntity();

        transaction.setAmount(createTransactionDTO.amount());
        transaction.setTransactionType(
                createTransactionDTO.transactionType());
        transaction.setCategory(category);
        transaction.setDate(createTransactionDTO.date());
        transaction.setDescription(
                createTransactionDTO.description());

        TransactionEntity savedTransaction = transactionRepository.save(transaction);

        List<TransactionSplitEntity> splitEntities = new ArrayList<>();

        for (int i = 0; i < splits.size(); i++) {
            CreateTransactionSplitDTO splitDTO = splits.get(i);

            TransactionSplitEntity split = new TransactionSplitEntity();

            split.setTransaction(savedTransaction);
            split.setSubcategory(subcategories.get(i));
            split.setAmount(splitDTO.amount());

            splitEntities.add(split);
        }

        if (!splitEntities.isEmpty()) {
            transactionSplitRepository.saveAll(splitEntities);
        }

        return mapToResponseDTO(savedTransaction);
    }

    @Transactional
    public TransactionResponseDTO updateTransaction(
            UUID transactionId,
            UpdateTransactionDTO updateTransactionDTO) {

        TransactionEntity transaction = getTransactionOrThrow(transactionId);

        List<TransactionSplitEntity> splits = transactionSplitRepository
                .findByTransactionId(transactionId);

        BigDecimal amount = transaction.getAmount();

        if (updateTransactionDTO.amount() != null) {
            amount = updateTransactionDTO.amount();
        }

        BigDecimal splitTotal = splits.stream()
                .map(split -> Objects.requireNonNull(split.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (splitTotal.compareTo(amount) > 0) {
            throw new BadRequestException(
                    "The transaction amount cannot be lower than the total split amount");
        }

        CategoryEntity category = transaction.getCategory();

        if (updateTransactionDTO.categoryId() != null) {
            UUID newCategoryId = updateTransactionDTO.categoryId();

            if (!newCategoryId.equals(transaction.getCategory().getId())
                    && !splits.isEmpty()) {

                throw new BadRequestException(
                        "The transaction category cannot be changed while the transaction has splits");
            }

            category = categoryRepository
                    .findById(newCategoryId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "The category id "
                                    + newCategoryId
                                    + " was not found"));
        }

        transaction.setAmount(amount);
        transaction.setCategory(category);

        if (updateTransactionDTO.transactionType() != null) {
            transaction.setTransactionType(
                    updateTransactionDTO.transactionType());
        }

        if (updateTransactionDTO.date() != null) {
            transaction.setDate(updateTransactionDTO.date());
        }

        if (updateTransactionDTO.description() != null) {
            transaction.setDescription(
                    updateTransactionDTO.description());
        }

        TransactionEntity updatedTransaction = transactionRepository.save(transaction);

        return mapToResponseDTO(updatedTransaction);
    }

    @Transactional
    public void deleteTransaction(UUID transactionId) {
        TransactionEntity transaction = getTransactionOrThrow(transactionId);

        transactionSplitRepository
                .deleteByTransactionId(transactionId);

        transactionRepository.delete(transaction);
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

    // Mapper methods

    private TransactionResponseDTO mapToResponseDTO(
            TransactionEntity transaction) {

        List<TransactionSplitResponseDTO> splits = transactionSplitRepository
                .findByTransactionId(transaction.getId())
                .stream()
                .map(this::mapSplitToResponseDTO)
                .toList();

        return new TransactionResponseDTO(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getTransactionType(),
                transaction.getCategory().getId(),
                transaction.getDate(),
                transaction.getDescription(),
                splits,
                transaction.getCreatedAt());
    }

    private TransactionSplitResponseDTO mapSplitToResponseDTO(
            TransactionSplitEntity split) {

        return new TransactionSplitResponseDTO(
                split.getId(),
                split.getSubcategory().getId(),
                split.getAmount());
    }
}