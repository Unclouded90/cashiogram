package com.mauro.cashiogram.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mauro.cashiogram.category.entity.CategoryEntity;
import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.exception.ResourceNotFoundException;
import com.mauro.cashiogram.subcategory.entity.SubcategoryEntity;
import com.mauro.cashiogram.subcategory.repository.SubcategoryRepository;
import com.mauro.cashiogram.transaction.dto.CreateTransactionSplitDTO;
import com.mauro.cashiogram.transaction.dto.TransactionSplitResponseDTO;
import com.mauro.cashiogram.transaction.dto.UpdateTransactionSplitDTO;
import com.mauro.cashiogram.transaction.entity.TransactionEntity;
import com.mauro.cashiogram.transaction.entity.TransactionSplitEntity;
import com.mauro.cashiogram.transaction.entity.TransactionType;
import com.mauro.cashiogram.transaction.repository.TransactionRepository;
import com.mauro.cashiogram.transaction.repository.TransactionSplitRepository;

@ExtendWith(MockitoExtension.class)
class TransactionSplitServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionSplitRepository transactionSplitRepository;

    @Mock
    private SubcategoryRepository subcategoryRepository;

    private TransactionSplitService transactionSplitService;

    @BeforeEach
    void setUp() {
        transactionSplitService = new TransactionSplitService(
                transactionRepository,
                transactionSplitRepository,
                subcategoryRepository);
    }

    @Test
    void shouldCreateSplit() {
        CategoryEntity category = createCategory();

        TransactionEntity transaction = createTransaction(
                category,
                new BigDecimal("100.00"));

        SubcategoryEntity subcategory = createSubcategory(category);

        CreateTransactionSplitDTO createDTO = new CreateTransactionSplitDTO(
                subcategory.getId(),
                new BigDecimal("30.00"));

        when(transactionRepository.findById(transaction.getId()))
                .thenReturn(Optional.of(transaction));

        when(subcategoryRepository.findById(subcategory.getId()))
                .thenReturn(Optional.of(subcategory));

        when(transactionSplitRepository
                .existsByTransactionIdAndSubcategoryId(
                        transaction.getId(),
                        subcategory.getId()))
                .thenReturn(false);

        when(transactionSplitRepository
                .findByTransactionId(transaction.getId()))
                .thenReturn(List.of());

        when(transactionSplitRepository
                .save(any(TransactionSplitEntity.class)))
                .thenAnswer(invocation -> {
                    TransactionSplitEntity split = invocation.getArgument(0);

                    split.setId(UUID.randomUUID());

                    return split;
                });

        TransactionSplitResponseDTO result = transactionSplitService.createSplit(
                transaction.getId(),
                createDTO);

        assertEquals(
                subcategory.getId(),
                result.subcategoryId());

        assertEquals(
                new BigDecimal("30.00"),
                result.amount());

        verify(transactionSplitRepository)
                .save(any(TransactionSplitEntity.class));
    }

    @Test
    void shouldFailCreateWhenTransactionDoesNotExist() {
        UUID transactionId = UUID.randomUUID();

        CreateTransactionSplitDTO createDTO = new CreateTransactionSplitDTO(
                UUID.randomUUID(),
                new BigDecimal("30.00"));

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionSplitService.createSplit(
                        transactionId,
                        createDTO));

        verify(subcategoryRepository, never())
                .findById(any());

        verify(transactionSplitRepository, never())
                .save(any());
    }

    @Test
    void shouldFailCreateWhenSubcategoryDoesNotExist() {
        CategoryEntity category = createCategory();

        TransactionEntity transaction = createTransaction(
                category,
                new BigDecimal("100.00"));

        UUID subcategoryId = UUID.randomUUID();

        CreateTransactionSplitDTO createDTO = new CreateTransactionSplitDTO(
                subcategoryId,
                new BigDecimal("30.00"));

        when(transactionRepository.findById(transaction.getId()))
                .thenReturn(Optional.of(transaction));

        when(subcategoryRepository.findById(subcategoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionSplitService.createSplit(
                        transaction.getId(),
                        createDTO));

        verify(transactionSplitRepository, never())
                .save(any());
    }

    @Test
    void shouldFailCreateWhenSubcategoryDoesNotBelongToTransactionCategory() {
        CategoryEntity transactionCategory = createCategory();

        CategoryEntity differentCategory = createCategory();

        TransactionEntity transaction = createTransaction(
                transactionCategory,
                new BigDecimal("100.00"));

        SubcategoryEntity subcategory = createSubcategory(differentCategory);

        CreateTransactionSplitDTO createDTO = new CreateTransactionSplitDTO(
                subcategory.getId(),
                new BigDecimal("30.00"));

        when(transactionRepository.findById(transaction.getId()))
                .thenReturn(Optional.of(transaction));

        when(subcategoryRepository.findById(subcategory.getId()))
                .thenReturn(Optional.of(subcategory));

        assertThrows(
                BadRequestException.class,
                () -> transactionSplitService.createSplit(
                        transaction.getId(),
                        createDTO));

        verify(transactionSplitRepository, never())
                .save(any());
    }

    @Test
    void shouldFailCreateWhenSubcategoryAlreadyHasSplit() {
        CategoryEntity category = createCategory();

        TransactionEntity transaction = createTransaction(
                category,
                new BigDecimal("100.00"));

        SubcategoryEntity subcategory = createSubcategory(category);

        CreateTransactionSplitDTO createDTO = new CreateTransactionSplitDTO(
                subcategory.getId(),
                new BigDecimal("30.00"));

        when(transactionRepository.findById(transaction.getId()))
                .thenReturn(Optional.of(transaction));

        when(subcategoryRepository.findById(subcategory.getId()))
                .thenReturn(Optional.of(subcategory));

        when(transactionSplitRepository
                .existsByTransactionIdAndSubcategoryId(
                        transaction.getId(),
                        subcategory.getId()))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> transactionSplitService.createSplit(
                        transaction.getId(),
                        createDTO));

        verify(transactionSplitRepository, never())
                .save(any());
    }

    @Test
    void shouldFailCreateWhenSplitTotalExceedsTransactionAmount() {
        CategoryEntity category = createCategory();

        TransactionEntity transaction = createTransaction(
                category,
                new BigDecimal("100.00"));

        SubcategoryEntity existingSubcategory = createSubcategory(category);

        SubcategoryEntity newSubcategory = createSubcategory(category);

        TransactionSplitEntity existingSplit = createSplit(
                transaction,
                existingSubcategory,
                new BigDecimal("80.00"));

        CreateTransactionSplitDTO createDTO = new CreateTransactionSplitDTO(
                newSubcategory.getId(),
                new BigDecimal("30.00"));

        when(transactionRepository.findById(transaction.getId()))
                .thenReturn(Optional.of(transaction));

        when(subcategoryRepository.findById(newSubcategory.getId()))
                .thenReturn(Optional.of(newSubcategory));

        when(transactionSplitRepository
                .existsByTransactionIdAndSubcategoryId(
                        transaction.getId(),
                        newSubcategory.getId()))
                .thenReturn(false);

        when(transactionSplitRepository
                .findByTransactionId(transaction.getId()))
                .thenReturn(List.of(existingSplit));

        assertThrows(
                BadRequestException.class,
                () -> transactionSplitService.createSplit(
                        transaction.getId(),
                        createDTO));

        verify(transactionSplitRepository, never())
                .save(any());
    }

    @Test
    void shouldUpdateSplit() {
        CategoryEntity category = createCategory();

        TransactionEntity transaction = createTransaction(
                category,
                new BigDecimal("100.00"));

        SubcategoryEntity oldSubcategory = createSubcategory(category);

        SubcategoryEntity newSubcategory = createSubcategory(category);

        TransactionSplitEntity split = createSplit(
                transaction,
                oldSubcategory,
                new BigDecimal("30.00"));

        UpdateTransactionSplitDTO updateDTO = new UpdateTransactionSplitDTO(
                newSubcategory.getId(),
                new BigDecimal("40.00"));

        when(transactionSplitRepository
                .findByIdAndTransactionId(
                        split.getId(),
                        transaction.getId()))
                .thenReturn(Optional.of(split));

        when(subcategoryRepository
                .findById(newSubcategory.getId()))
                .thenReturn(Optional.of(newSubcategory));

        when(transactionSplitRepository
                .existsByTransactionIdAndSubcategoryIdAndIdNot(
                        transaction.getId(),
                        newSubcategory.getId(),
                        split.getId()))
                .thenReturn(false);

        when(transactionSplitRepository
                .findByTransactionId(transaction.getId()))
                .thenReturn(List.of(split));

        when(transactionSplitRepository.save(split))
                .thenReturn(split);

        TransactionSplitResponseDTO result = transactionSplitService.updateSplit(
                transaction.getId(),
                split.getId(),
                updateDTO);

        assertEquals(
                newSubcategory.getId(),
                result.subcategoryId());

        assertEquals(
                new BigDecimal("40.00"),
                result.amount());

        verify(transactionSplitRepository)
                .save(split);
    }

    @Test
    void shouldFailUpdateWhenSplitDoesNotExist() {
        UUID transactionId = UUID.randomUUID();
        UUID splitId = UUID.randomUUID();

        UpdateTransactionSplitDTO updateDTO = new UpdateTransactionSplitDTO(
                null,
                new BigDecimal("40.00"));

        when(transactionSplitRepository
                .findByIdAndTransactionId(
                        splitId,
                        transactionId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionSplitService.updateSplit(
                        transactionId,
                        splitId,
                        updateDTO));

        verify(transactionSplitRepository, never())
                .save(any());
    }

    @Test
    void shouldFailUpdateWhenNewSubcategoryDoesNotExist() {
        CategoryEntity category = createCategory();

        TransactionEntity transaction = createTransaction(
                category,
                new BigDecimal("100.00"));

        SubcategoryEntity currentSubcategory = createSubcategory(category);

        TransactionSplitEntity split = createSplit(
                transaction,
                currentSubcategory,
                new BigDecimal("30.00"));

        UUID newSubcategoryId = UUID.randomUUID();

        UpdateTransactionSplitDTO updateDTO = new UpdateTransactionSplitDTO(
                newSubcategoryId,
                null);

        when(transactionSplitRepository
                .findByIdAndTransactionId(
                        split.getId(),
                        transaction.getId()))
                .thenReturn(Optional.of(split));

        when(subcategoryRepository.findById(newSubcategoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionSplitService.updateSplit(
                        transaction.getId(),
                        split.getId(),
                        updateDTO));

        verify(transactionSplitRepository, never())
                .save(any());
    }

    @Test
    void shouldFailUpdateWhenNewSubcategoryDoesNotBelongToTransactionCategory() {
        CategoryEntity transactionCategory = createCategory();

        CategoryEntity differentCategory = createCategory();

        TransactionEntity transaction = createTransaction(
                transactionCategory,
                new BigDecimal("100.00"));

        SubcategoryEntity oldSubcategory = createSubcategory(transactionCategory);

        SubcategoryEntity newSubcategory = createSubcategory(differentCategory);

        TransactionSplitEntity split = createSplit(
                transaction,
                oldSubcategory,
                new BigDecimal("30.00"));

        UpdateTransactionSplitDTO updateDTO = new UpdateTransactionSplitDTO(
                newSubcategory.getId(),
                null);

        when(transactionSplitRepository
                .findByIdAndTransactionId(
                        split.getId(),
                        transaction.getId()))
                .thenReturn(Optional.of(split));

        when(subcategoryRepository
                .findById(newSubcategory.getId()))
                .thenReturn(Optional.of(newSubcategory));

        assertThrows(
                BadRequestException.class,
                () -> transactionSplitService.updateSplit(
                        transaction.getId(),
                        split.getId(),
                        updateDTO));

        verify(transactionSplitRepository, never())
                .save(any());
    }

    @Test
    void shouldFailUpdateWhenSubcategoryWouldBeDuplicated() {
        CategoryEntity category = createCategory();

        TransactionEntity transaction = createTransaction(
                category,
                new BigDecimal("100.00"));

        SubcategoryEntity oldSubcategory = createSubcategory(category);

        SubcategoryEntity newSubcategory = createSubcategory(category);

        TransactionSplitEntity split = createSplit(
                transaction,
                oldSubcategory,
                new BigDecimal("30.00"));

        UpdateTransactionSplitDTO updateDTO = new UpdateTransactionSplitDTO(
                newSubcategory.getId(),
                null);

        when(transactionSplitRepository
                .findByIdAndTransactionId(
                        split.getId(),
                        transaction.getId()))
                .thenReturn(Optional.of(split));

        when(subcategoryRepository
                .findById(newSubcategory.getId()))
                .thenReturn(Optional.of(newSubcategory));

        when(transactionSplitRepository
                .existsByTransactionIdAndSubcategoryIdAndIdNot(
                        transaction.getId(),
                        newSubcategory.getId(),
                        split.getId()))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> transactionSplitService.updateSplit(
                        transaction.getId(),
                        split.getId(),
                        updateDTO));

        verify(transactionSplitRepository, never())
                .save(any());
    }

    @Test
    void shouldFailUpdateWhenSplitTotalExceedsTransactionAmount() {
        CategoryEntity category = createCategory();

        TransactionEntity transaction = createTransaction(
                category,
                new BigDecimal("100.00"));

        SubcategoryEntity firstSubcategory = createSubcategory(category);

        SubcategoryEntity secondSubcategory = createSubcategory(category);

        TransactionSplitEntity splitToUpdate = createSplit(
                transaction,
                firstSubcategory,
                new BigDecimal("30.00"));

        TransactionSplitEntity otherSplit = createSplit(
                transaction,
                secondSubcategory,
                new BigDecimal("60.00"));

        UpdateTransactionSplitDTO updateDTO = new UpdateTransactionSplitDTO(
                null,
                new BigDecimal("50.00"));

        when(transactionSplitRepository
                .findByIdAndTransactionId(
                        splitToUpdate.getId(),
                        transaction.getId()))
                .thenReturn(Optional.of(splitToUpdate));

        when(transactionSplitRepository
                .existsByTransactionIdAndSubcategoryIdAndIdNot(
                        transaction.getId(),
                        firstSubcategory.getId(),
                        splitToUpdate.getId()))
                .thenReturn(false);

        when(transactionSplitRepository
                .findByTransactionId(transaction.getId()))
                .thenReturn(
                        List.of(splitToUpdate, otherSplit));

        assertThrows(
                BadRequestException.class,
                () -> transactionSplitService.updateSplit(
                        transaction.getId(),
                        splitToUpdate.getId(),
                        updateDTO));

        verify(transactionSplitRepository, never())
                .save(any());
    }

    @Test
    void shouldDeleteSplit() {
        CategoryEntity category = createCategory();

        TransactionEntity transaction = createTransaction(
                category,
                new BigDecimal("100.00"));

        SubcategoryEntity subcategory = createSubcategory(category);

        TransactionSplitEntity split = createSplit(
                transaction,
                subcategory,
                new BigDecimal("30.00"));

        when(transactionSplitRepository
                .findByIdAndTransactionId(
                        split.getId(),
                        transaction.getId()))
                .thenReturn(Optional.of(split));

        transactionSplitService.deleteSplit(
                transaction.getId(),
                split.getId());

        verify(transactionSplitRepository)
                .delete(split);
    }

    @Test
    void shouldFailDeleteWhenSplitDoesNotExist() {
        UUID transactionId = UUID.randomUUID();
        UUID splitId = UUID.randomUUID();

        when(transactionSplitRepository
                .findByIdAndTransactionId(
                        splitId,
                        transactionId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionSplitService.deleteSplit(
                        transactionId,
                        splitId));

        verify(transactionSplitRepository, never())
                .delete(any());
    }

    // Helper methods

    private CategoryEntity createCategory() {
        CategoryEntity category = new CategoryEntity();

        category.setId(UUID.randomUUID());
        category.setName("Category");

        return category;
    }

    private SubcategoryEntity createSubcategory(
            CategoryEntity category) {

        SubcategoryEntity subcategory = new SubcategoryEntity();

        subcategory.setId(UUID.randomUUID());
        subcategory.setName("Subcategory");
        subcategory.setCategory(category);

        return subcategory;
    }

    private TransactionEntity createTransaction(
            CategoryEntity category,
            BigDecimal amount) {

        TransactionEntity transaction = new TransactionEntity();

        transaction.setId(UUID.randomUUID());
        transaction.setAmount(amount);
        transaction.setTransactionType(
                TransactionType.EXPENSE);
        transaction.setCategory(category);
        transaction.setDate(LocalDate.now());
        transaction.setDescription("Test transaction");

        return transaction;
    }

    private TransactionSplitEntity createSplit(
            TransactionEntity transaction,
            SubcategoryEntity subcategory,
            BigDecimal amount) {

        TransactionSplitEntity split = new TransactionSplitEntity();

        split.setId(UUID.randomUUID());
        split.setTransaction(transaction);
        split.setSubcategory(subcategory);
        split.setAmount(amount);

        return split;
    }
}