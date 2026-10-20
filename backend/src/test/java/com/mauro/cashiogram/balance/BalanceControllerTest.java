package com.mauro.cashiogram.balance;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mauro.cashiogram.balance.dto.BalanceResponseDTO;
import com.mauro.cashiogram.exception.ResourceNotFoundException;

@WebMvcTest(BalanceController.class)
class BalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BalanceService balanceService;

    @Test
    void shouldGetBalance() throws Exception {
        BalanceResponseDTO response = new BalanceResponseDTO(
                new BigDecimal("1000.00"),
                new BigDecimal("2200.00"),
                new BigDecimal("850.00"),
                new BigDecimal("2350.00"));

        when(balanceService.getBalance())
                .thenReturn(response);

        mockMvc.perform(
                get("/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initialBalance")
                        .value(1000.00))
                .andExpect(jsonPath("$.totalIncome")
                        .value(2200.00))
                .andExpect(jsonPath("$.totalExpenses")
                        .value(850.00))
                .andExpect(jsonPath("$.currentBalance")
                        .value(2350.00));

        verify(balanceService)
                .getBalance();
    }

    @Test
    void shouldReturnNotFoundWhenAccountIsNotInitialized()
            throws Exception {

        when(balanceService.getBalance())
                .thenThrow(
                        new ResourceNotFoundException(
                                "Account has not been initialized"));

        mockMvc.perform(
                get("/balance"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Account has not been initialized"));
    }
}