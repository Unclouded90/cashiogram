package com.mauro.cashiogram.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
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
import com.mauro.cashiogram.category.repository.CategoryRepository;
import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.exception.ResourceNotFoundException;
import com.mauro.cashiogram.subcategory.entity.SubcategoryEntity;
import com.mauro.cashiogram.subcategory.repository.SubcategoryRepository;
import com.mauro.cashiogram.transaction.dto.CreateTransactionDTO;
import com.mauro.cashiogram.transaction.dto.CreateTransactionSplitDTO;
import com.mauro.cashiogram.transaction.dto.TransactionResponseDTO;
import com.mauro.cashiogram.transaction.dto.UpdateTransactionDTO;
import com.mauro.cashiogram.transaction.entity.TransactionEntity;
import com.mauro.cashiogram.transaction.entity.TransactionSplitEntity;
import com.mauro.cashiogram.transaction.entity.TransactionType;
import com.mauro.cashiogram.transaction.repository.TransactionRepository;
import com.mauro.cashiogram.transaction.repository.TransactionSplitRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionSplitRepository transactionSplitRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubcategoryRepository subcategoryRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(
                transactionRepository,
                transactionSplitRepository,
                categoryRepository,
                subcategoryRepository);
    }

    @Test
    void shouldGetAllTransactionsOrderedByDateDescending() {
        CategoryEntity category = createCategory();

        TransactionEntity newestTransaction = createTransaction(
                category,
                new BigDecimal("100.00"),
                LocalDate.of(2026, 10, 11));

        TransactionEntity oldestTransaction = createTransaction(
                category,
                new BigDecimal("50.00"),
                LocalDate.of(2026, 10, 10));

        when(transactionRepository.findAllByOrderByDateDesc())
                .thenReturn(List.of(
                        newestTransaction,
                        oldestTransaction));

        when(transactionSplitRepository.findByTransactionId(
                newestTransaction.getId()))
                .thenReturn(List.of());

        when(transactionSplitRepository.findByTransactionId(
                oldestTransaction.getId()))
                .thenReturn(List.of());

        List<TransactionResponseDTO> result = transactionService.getTransactions();

        assertEquals(2, result.size());

        assertEquals(
                LocalDate.of(2026, 10, 11),
                result.get(0).date());

        assertEquals(
                LocalDate.of(2026, 10, 10),
                result.get(1).date());

        verify(transactionRepository)
                .findAllByOrderByDateDesc();
    }

    @Test
    void shouldGetTransactionById() {
        CategoryEntity category = createCategory();
        TransactionEntity transaction = createTransaction(
                category,
                new BigDecimal("100.00"),
                LocalDate.now());

        when(transactionRepository.findById(transaction.getId()))
                .thenReturn(Optional.of(transaction));

        when(transactionSplitRepository.findByTransactionId(
                transaction.getId()))
                .thenReturn(List.of());

        TransactionResponseDTO result = transactionService.getTransactionById(
                transaction.getId());

        assertEquals(transaction.getId(), result.id());
        assertEquals(
                new BigDecimal("100.00"),
                result.amount());
        assertEquals(category.getId(), result.categoryId());
    }

    @Test
    void shouldThrowWhenTransactionDoesNotExist() {
        UUID transactionId = UUID.randomUUID();

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService
                        .getTransactionById(transactionId));

        verify(transactionSplitRepository, never())
                .findByTransactionId(any());
    }

    @Test
    void shouldCreateTransactionWithoutSplits() {
        UUID transactionId = UUID.randomUUID();
        CategoryEntity category = createCategory();

        CreateTransactionDTO createDTO = new CreateTransactionDTO(
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                category.getId(),
                LocalDate.of(2026, 10, 11),
                "Continente",
                List.of());

        when(categoryRepository.findById(category.getId()))
                .thenReturn(Optional.of(category));

        when(transactionRepository.save(
                any(TransactionEntity.class)))
                .thenAnswer(invocation -> {
                    TransactionEntity transaction = invocation.getArgument(0);

                    transaction.setId(transactionId);
                    transaction.setCreatedAt(Instant.now());

                    return transaction;
                });

        when(transactionSplitRepository
                .findByTransactionId(transactionId))
                .thenReturn(List.of());

        TransactionResponseDTO result = transactionService.createTransaction(createDTO);

        assertEquals(transactionId, result.id());
        assertEquals(
                new BigDecimal("100.00"),
                result.amount());
        assertEquals(
                TransactionType.EXPENSE,
                result.transactionType());
        assertEquals(category.getId(), result.categoryId());
        assertTrue(result.splits().isEmpty());

        verify(transactionSplitRepository, never())
                .saveAll(anyList());
    }

    @Test
    void shouldCreateTransactionWithSplits() {
        UUID transactionId = UUID.randomUUID();

        CategoryEntity category = createCategory();

        SubcategoryEntity food = createSubcategory(category);

        SubcategoryEntity cats = createSubcategory(category);

        CreateTransactionSplitDTO foodSplit = new CreateTransactionSplitDTO(
                food.getId(),
                new BigDecimal("60.00"));

        CreateTransactionSplitDTO catsSplit = new CreateTransactionSplitDTO(
                cats.getId(),
                new BigDecimal("20.00"));

        CreateTransactionDTO createDTO = new CreateTransactionDTO(
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                category.getId(),
                LocalDate.of(2026, 10, 11),
                "Continente",
                List.of(foodSplit, catsSplit));

        when(categoryRepository.findById(category.getId()))
                .thenReturn(Optional.of(category));

        when(subcategoryRepository.findById(food.getId()))
                .thenReturn(Optional.of(food));

        when(subcategoryRepository.findById(cats.getId()))
                .thenReturn(Optional.of(cats));

        when(transactionRepository.save(
                any(TransactionEntity.class)))
                .thenAnswer(invocation -> {
                    TransactionEntity transaction = invocation.getArgument(0);

                    transaction.setId(transactionId);
                    transaction.setCreatedAt(Instant.now());

                    return transaction;
                });

        TransactionSplitEntity savedFoodSplit = createSplit(
                transactionId,
                food,
                new BigDecimal("60.00"));

        TransactionSplitEntity savedCatsSplit = createSplit(
                transactionId,
                cats,
                new BigDecimal("20.00"));

        when(transactionSplitRepository
                .findByTransactionId(transactionId))
                .thenReturn(
                        List.of(savedFoodSplit, savedCatsSplit));

        TransactionResponseDTO result = transactionService.createTransaction(createDTO);

        assertEquals(transactionId, result.id());
        assertEquals(2, result.splits().size());

        assertEquals(
                food.getId(),
                result.splits().get(0).subcategoryId());

        assertEquals(
                new BigDecimal("60.00"),
                result.splits().get(0).amount());

        verify(transactionSplitRepository)
                .saveAll(anyList());
    }

    @Test
    void shouldFailCreateWhenCategoryDoesNotExist() {
        UUID categoryId = UUID.randomUUID();

        CreateTransactionDTO createDTO = new CreateTransactionDTO(
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                categoryId,
                LocalDate.now(),
                "Continente",
                List.of());

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService
                        .createTransaction(createDTO));

        verify(transactionRepository, never())
                .save(any());
    }

    @Test
    void shouldFailCreateWhenSubcategoryDoesNotExist() {
        CategoryEntity category = createCategory();
        UUID subcategoryId = UUID.randomUUID();

        CreateTransactionDTO createDTO = new CreateTransactionDTO(
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                category.getId(),
                LocalDate.now(),
                "Continente",
                List.of(
                        new CreateTransactionSplitDTO(
                                subcategoryId,
                                new BigDecimal("20.00"))));

        when(categoryRepository.findById(category.getId()))
                .thenReturn(Optional.of(category));

        when(subcategoryRepository.findById(subcategoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService
                        .createTransaction(createDTO));

        verify(transactionRepository, never())
                .save(any());
    }

    @Test
    void shouldFailCreateWhenSubcategoryDoesNotBelongToCategory() {
        CategoryEntity transactionCategory = createCategory();

        CategoryEntity differentCategory = createCategory();

        SubcategoryEntity subcategory = createSubcategory(differentCategory);

        CreateTransactionDTO createDTO = new CreateTransactionDTO(
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                transactionCategory.getId(),
                LocalDate.now(),
                "Continente",
                List.of(
                        new CreateTransactionSplitDTO(
                                subcategory.getId(),
                                new BigDecimal("20.00"))));

        when(categoryRepository.findById(
                transactionCategory.getId()))
                .thenReturn(
                        Optional.of(transactionCategory));

        when(subcategoryRepository.findById(
                subcategory.getId()))
                .thenReturn(Optional.of(subcategory));

        assertThrows(
                BadRequestException.class,
                () -> transactionService
                        .createTransaction(createDTO));

        verify(transactionRepository, never())
                .save(any());
    }

    @Test
    void shouldFailCreateWhenSubcategoryIsRepeated() {
        CategoryEntity category = createCategory();
        SubcategoryEntity subcategory = createSubcategory(category);

        CreateTransactionDTO createDTO = new CreateTransactionDTO(
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                category.getId(),
                LocalDate.now(),
                "Continente",
                List.of(
                        new CreateTransactionSplitDTO(
                                subcategory.getId(),
                                new BigDecimal("20.00")),
                        new CreateTransactionSplitDTO(
                                subcategory.getId(),
                                new BigDecimal("30.00"))));

        when(categoryRepository.findById(category.getId()))
                .thenReturn(Optional.of(category));

        when(subcategoryRepository.findById(
                subcategory.getId()))
                .thenReturn(Optional.of(subcategory));

        assertThrows(
                BadRequestException.class,
                () -> transactionService
                        .createTransaction(createDTO));

        verify(transactionRepository, never())
                .save(any());
    }

    @Test
    void shouldFailCreateWhenSplitTotalExceedsTransactionAmount() {
        CategoryEntity category = createCategory();

        SubcategoryEntity firstSubcategory = createSubcategory(category);

        SubcategoryEntity secondSubcategory = createSubcategory(category);

        CreateTransactionDTO createDTO = new CreateTransactionDTO(
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                category.getId(),
                LocalDate.now(),
                "Continente",
                List.of(
                        new CreateTransactionSplitDTO(
                                firstSubcategory.getId(),
                                new BigDecimal("60.00")),
                        new CreateTransactionSplitDTO(
                                secondSubcategory.getId(),
                                new BigDecimal("50.00"))));

        when(categoryRepository.findById(category.getId()))
                .thenReturn(Optional.of(category));

        when(subcategoryRepository.findById(
                firstSubcategory.getId()))
                .thenReturn(Optional.of(firstSubcategory));

        when(subcategoryRepository.findById(
                secondSubcategory.getId()))
                .thenReturn(Optional.of(secondSubcategory));

        assertThrows(
                BadRequestException.class,
                () -> transactionService
                        .createTransaction(createDTO));

        verify(transactionRepository, never())
                .save(any());
    }

    @Test
    void shouldUpdateTransaction() {
        CategoryEntity category = createCategory();

        TransactionEntity transaction = createTransaction(
                category,
                new BigDecimal("100.00"),
                LocalDate.of(2026, 10, 10));

        UpdateTransactionDTO updateDTO = new UpdateTransactionDTO(
                new BigDecimal("120.00"),
                TransactionType.EXPENSE,
                null,
                LocalDate.of(2026, 10, 11),
                "Updated description");

        when(transactionRepository.findById(
                transaction.getId()))
                .thenReturn(Optional.of(transaction));

        when(transactionSplitRepository
                .findByTransactionId(transaction.getId()))
                .thenReturn(List.of());

        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        TransactionResponseDTO result = transactionService.updateTransaction(
                transaction.getId(),
                updateDTO);

        assertEquals(
                new BigDecimal("120.00"),
                result.amount());

        assertEquals(
                LocalDate.of(2026, 10, 11),
                result.date());

        assertEquals(
                "Updated description",
                result.description());

        verify(transactionRepository)
                .save(transaction);
    }

    @Test
    void shouldFailUpdateWhenTransactionDoesNotExist() {
        UUID transactionId = UUID.randomUUID();

        UpdateTransactionDTO updateDTO = new UpdateTransactionDTO(
                new BigDecimal("100.00"),
                null,
                null,
                null,
                null);

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.updateTransaction(
                        transactionId,
                        updateDTO));

        verify(transactionRepository, never())
                .save(any());
    }

    @Test
    void shouldFailUpdateWhenNewCategoryDoesNotExist() {
        CategoryEntity category = createCategory();

        TransactionEntity transaction = createTransaction(
                category,
                new BigDecimal("100.00"),
                LocalDate.now());

        UUID newCategoryId = UUID.randomUUID();

        UpdateTransactionDTO updateDTO = new UpdateTransactionDTO(
                null,
                null,
                newCategoryId,
                null,
                null);

        when(transactionRepository.findById(
                transaction.getId()))
                .thenReturn(Optional.of(transaction));

        when(categoryRepository.findById(newCategoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.updateTransaction(
                        transaction.getId(),
                        updateDTO));

        verify(transactionRepository, never())
                .save(any());
    }

    @Test
    void shouldFailUpdateWhenAmountIsLowerThanExistingSplits() {
        CategoryEntity category = createCategory();

        TransactionEntity transaction = createTransaction(
                category,
                new BigDecimal("100.00"),
                LocalDate.now());

        SubcategoryEntity subcategory = createSubcategory(category);

        TransactionSplitEntity split = createSplit(
                transaction.getId(),
                subcategory,
                new BigDecimal("80.00"));

        UpdateTransactionDTO updateDTO = new UpdateTransactionDTO(
                new BigDecimal("60.00"),
                null,
                null,
                null,
                null);

        when(transactionRepository.findById(
                transaction.getId()))
                .thenReturn(Optional.of(transaction));

        when(transactionSplitRepository
                .findByTransactionId(transaction.getId()))
                .thenReturn(List.of(split));

        assertThrows(
                BadRequestException.class,
                () -> transactionService.updateTransaction(
                        transaction.getId(),
                        updateDTO));

        verify(transactionRepository, never())
                .save(any());
    }

    @Test
    void shouldFailUpdateWhenExistingSplitsDoNotBelongToNewCategory() {
        CategoryEntity oldCategory = createCategory();

        CategoryEntity newCategory = createCategory();

        TransactionEntity transaction = createTransaction(
                oldCategory,
                new BigDecimal("100.00"),
                LocalDate.now());

        SubcategoryEntity oldSubcategory = createSubcategory(oldCategory);

        TransactionSplitEntity split = createSplit(
                transaction.getId(),
                oldSubcategory,
                new BigDecimal("50.00"));

        UpdateTransactionDTO updateDTO = new UpdateTransactionDTO(
                null,
                null,
                newCategory.getId(),
                null,
                null);

        when(transactionRepository.findById(
                transaction.getId()))
                .thenReturn(Optional.of(transaction));

        when(categoryRepository.findById(
                newCategory.getId()))
                .thenReturn(Optional.of(newCategory));

        when(transactionSplitRepository
                .findByTransactionId(transaction.getId()))
                .thenReturn(List.of(split));

        assertThrows(
                BadRequestException.class,
                () -> transactionService.updateTransaction(
                        transaction.getId(),
                        updateDTO));

        verify(transactionRepository, never())
                .save(any());
    }

    @Test
    void shouldDeleteTransaction() {
        CategoryEntity category = createCategory();

        TransactionEntity transaction = createTransaction(
                category,
                new BigDecimal("100.00"),
                LocalDate.now());

        when(transactionRepository.findById(
                transaction.getId()))
                .thenReturn(Optional.of(transaction));

        transactionService.deleteTransaction(
                transaction.getId());

        verify(transactionSplitRepository)
                .deleteByTransactionId(
                        transaction.getId());

        verify(transactionRepository)
                .delete(transaction);
    }

    @Test
    void shouldFailDeleteWhenTransactionDoesNotExist() {
        UUID transactionId = UUID.randomUUID();

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService
                        .deleteTransaction(transactionId));

        verify(transactionSplitRepository, never())
                .deleteByTransactionId(any());

        verify(transactionRepository, never())
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
            BigDecimal amount,
            LocalDate date) {

        TransactionEntity transaction = new TransactionEntity();

        transaction.setId(UUID.randomUUID());
        transaction.setAmount(amount);
        transaction.setTransactionType(
                TransactionType.EXPENSE);
        transaction.setCategory(category);
        transaction.setDate(date);
        transaction.setDescription("Test transaction");
        transaction.setCreatedAt(Instant.now());

        return transaction;
    }

    private TransactionSplitEntity createSplit(
            UUID transactionId,
            SubcategoryEntity subcategory,
            BigDecimal amount) {

        TransactionEntity transaction = new TransactionEntity();

        transaction.setId(transactionId);

        TransactionSplitEntity split = new TransactionSplitEntity();

        split.setId(UUID.randomUUID());
        split.setTransaction(transaction);
        split.setSubcategory(subcategory);
        split.setAmount(amount);

        return split;
    }
}