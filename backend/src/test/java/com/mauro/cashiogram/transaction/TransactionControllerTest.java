package com.mauro.cashiogram.transaction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.exception.ResourceNotFoundException;
import com.mauro.cashiogram.transaction.dto.CreateTransactionDTO;
import com.mauro.cashiogram.transaction.dto.CreateTransactionSplitDTO;
import com.mauro.cashiogram.transaction.dto.TransactionResponseDTO;
import com.mauro.cashiogram.transaction.dto.TransactionSplitResponseDTO;
import com.mauro.cashiogram.transaction.dto.UpdateTransactionDTO;
import com.mauro.cashiogram.transaction.dto.UpdateTransactionSplitDTO;
import com.mauro.cashiogram.transaction.entity.TransactionType;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private TransactionSplitService transactionSplitService;

    @Test
    void shouldGetTransactions() throws Exception {
        TransactionResponseDTO transaction = createTransactionResponse();

        when(transactionService.getTransactions())
                .thenReturn(List.of(transaction));

        mockMvc.perform(
                get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(transaction.id().toString()))
                .andExpect(jsonPath("$[0].amount")
                        .value(100.00))
                .andExpect(jsonPath("$[0].transactionType")
                        .value("EXPENSE"))
                .andExpect(jsonPath("$[0].description")
                        .value("Continente"));

        verify(transactionService)
                .getTransactions();
    }

    @Test
    void shouldGetTransactionById() throws Exception {
        TransactionResponseDTO transaction = createTransactionResponse();

        when(transactionService.getTransactionById(
                transaction.id()))
                .thenReturn(transaction);

        mockMvc.perform(
                get("/transactions/{transactionId}",
                        transaction.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(transaction.id().toString()))
                .andExpect(jsonPath("$.amount")
                        .value(100.00))
                .andExpect(jsonPath("$.categoryId")
                        .value(transaction.categoryId().toString()));

        verify(transactionService)
                .getTransactionById(transaction.id());
    }

    @Test
    void shouldFailGetTransactionWhenNotFound()
            throws Exception {

        UUID transactionId = UUID.randomUUID();

        when(transactionService.getTransactionById(
                transactionId))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Transaction not found"));

        mockMvc.perform(
                get("/transactions/{transactionId}",
                        transactionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.message")
                        .value("Transaction not found"));
    }

    @Test
    void shouldCreateTransaction() throws Exception {
        UUID categoryId = UUID.randomUUID();

        CreateTransactionDTO createDTO = new CreateTransactionDTO(
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                categoryId,
                LocalDate.of(2026, 10, 11),
                "Continente",
                List.of());

        TransactionResponseDTO response = new TransactionResponseDTO(
                UUID.randomUUID(),
                createDTO.amount(),
                createDTO.transactionType(),
                categoryId,
                createDTO.date(),
                createDTO.description(),
                List.of(),
                Instant.now());

        when(transactionService.createTransaction(createDTO))
                .thenReturn(response);

        mockMvc.perform(
                post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                jsonMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(response.id().toString()))
                .andExpect(jsonPath("$.amount")
                        .value(100.00))
                .andExpect(jsonPath("$.transactionType")
                        .value("EXPENSE"));

        verify(transactionService)
                .createTransaction(createDTO);
    }

    @Test
    void shouldFailCreateTransactionWithInvalidAmount()
            throws Exception {

        CreateTransactionDTO createDTO = new CreateTransactionDTO(
                new BigDecimal("-10.00"),
                TransactionType.EXPENSE,
                UUID.randomUUID(),
                LocalDate.now(),
                "Invalid transaction",
                List.of());

        mockMvc.perform(
                post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                jsonMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());

        verify(transactionService, never())
                .createTransaction(any());
    }

    @Test
    void shouldUpdateTransaction() throws Exception {
        UUID transactionId = UUID.randomUUID();

        UpdateTransactionDTO updateDTO = new UpdateTransactionDTO(
                new BigDecimal("120.00"),
                null,
                null,
                null,
                "Updated transaction");

        TransactionResponseDTO response = new TransactionResponseDTO(
                transactionId,
                new BigDecimal("120.00"),
                TransactionType.EXPENSE,
                UUID.randomUUID(),
                LocalDate.now(),
                "Updated transaction",
                List.of(),
                Instant.now());

        when(transactionService.updateTransaction(
                transactionId,
                updateDTO))
                .thenReturn(response);

        mockMvc.perform(
                patch("/transactions/{transactionId}",
                        transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                jsonMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount")
                        .value(120.00))
                .andExpect(jsonPath("$.description")
                        .value("Updated transaction"));

        verify(transactionService)
                .updateTransaction(
                        transactionId,
                        updateDTO);
    }

    @Test
    void shouldReturnBadRequestWhenTransactionUpdateIsInvalid()
            throws Exception {

        UUID transactionId = UUID.randomUUID();

        UpdateTransactionDTO updateDTO = new UpdateTransactionDTO(
                new BigDecimal("50.00"),
                null,
                null,
                null,
                null);

        when(transactionService.updateTransaction(
                transactionId,
                updateDTO))
                .thenThrow(
                        new BadRequestException(
                                "Transaction amount cannot be lower than split total"));

        mockMvc.perform(
                patch("/transactions/{transactionId}",
                        transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                jsonMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Transaction amount cannot be lower than split total"));
    }

    @Test
    void shouldDeleteTransaction() throws Exception {
        UUID transactionId = UUID.randomUUID();

        mockMvc.perform(
                delete("/transactions/{transactionId}",
                        transactionId))
                .andExpect(status().isNoContent());

        verify(transactionService)
                .deleteTransaction(transactionId);
    }

    @Test
    void shouldCreateSplit() throws Exception {
        UUID transactionId = UUID.randomUUID();
        UUID subcategoryId = UUID.randomUUID();
        UUID splitId = UUID.randomUUID();

        CreateTransactionSplitDTO createDTO = new CreateTransactionSplitDTO(
                subcategoryId,
                new BigDecimal("30.00"));

        TransactionSplitResponseDTO response = new TransactionSplitResponseDTO(
                splitId,
                subcategoryId,
                new BigDecimal("30.00"));

        when(transactionSplitService.createSplit(
                transactionId,
                createDTO))
                .thenReturn(response);

        mockMvc.perform(
                post("/transactions/{transactionId}/splits",
                        transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                jsonMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(splitId.toString()))
                .andExpect(jsonPath("$.subcategoryId")
                        .value(subcategoryId.toString()))
                .andExpect(jsonPath("$.amount")
                        .value(30.00));

        verify(transactionSplitService)
                .createSplit(
                        transactionId,
                        createDTO);
    }

    @Test
    void shouldUpdateSplit() throws Exception {
        UUID transactionId = UUID.randomUUID();
        UUID splitId = UUID.randomUUID();
        UUID subcategoryId = UUID.randomUUID();

        UpdateTransactionSplitDTO updateDTO = new UpdateTransactionSplitDTO(
                null,
                new BigDecimal("40.00"));

        TransactionSplitResponseDTO response = new TransactionSplitResponseDTO(
                splitId,
                subcategoryId,
                new BigDecimal("40.00"));

        when(transactionSplitService.updateSplit(
                transactionId,
                splitId,
                updateDTO))
                .thenReturn(response);

        mockMvc.perform(
                patch(
                        "/transactions/{transactionId}/splits/{splitId}",
                        transactionId,
                        splitId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                jsonMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(splitId.toString()))
                .andExpect(jsonPath("$.amount")
                        .value(40.00));

        verify(transactionSplitService)
                .updateSplit(
                        transactionId,
                        splitId,
                        updateDTO);
    }

    @Test
    void shouldDeleteSplit() throws Exception {
        UUID transactionId = UUID.randomUUID();
        UUID splitId = UUID.randomUUID();

        mockMvc.perform(
                delete(
                        "/transactions/{transactionId}/splits/{splitId}",
                        transactionId,
                        splitId))
                .andExpect(status().isNoContent());

        verify(transactionSplitService)
                .deleteSplit(
                        transactionId,
                        splitId);
    }

    // Helper methods

    private TransactionResponseDTO createTransactionResponse() {
        return new TransactionResponseDTO(
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                UUID.randomUUID(),
                LocalDate.of(2026, 10, 11),
                "Continente",
                List.of(),
                Instant.now());
    }
}