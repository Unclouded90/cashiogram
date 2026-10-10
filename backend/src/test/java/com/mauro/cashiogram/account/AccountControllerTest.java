package com.mauro.cashiogram.account;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mauro.cashiogram.account.dto.AccountResponseDTO;
import com.mauro.cashiogram.account.dto.CreateAccountDTO;
import com.mauro.cashiogram.account.dto.UpdateAccountDTO;
import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.exception.ResourceNotFoundException;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @Test
    void itShouldGetAccount() throws Exception {
        UUID id = UUID.randomUUID();

        AccountResponseDTO response = new AccountResponseDTO(
                id,
                BigDecimal.valueOf(2000),
                Instant.now(),
                Instant.now());

        when(accountService.getAccount())
                .thenReturn(response);

        mockMvc.perform(get("/account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.initialBalance").value(2000));

        verify(accountService).getAccount();
    }

    @Test
    void itShouldReturn404WhenAccountDoesNotExist() throws Exception {
        when(accountService.getAccount())
                .thenThrow(
                        new ResourceNotFoundException(
                                "Account was not initialized or found"));

        mockMvc.perform(get("/account"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Account was not initialized or found"));

        verify(accountService).getAccount();
    }

    @Test
    void itShouldCreateAccount() throws Exception {
        CreateAccountDTO request = new CreateAccountDTO(BigDecimal.valueOf(3000));

        AccountResponseDTO response = new AccountResponseDTO(
                UUID.randomUUID(),
                BigDecimal.valueOf(3000),
                Instant.now(),
                Instant.now());

        when(accountService.createAccount(request))
                .thenReturn(response);

        mockMvc.perform(post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.initialBalance").value(3000));

        verify(accountService).createAccount(request);
    }

    @Test
    void itShouldReturn400WhenCreatingAccountAlreadyExists()
            throws Exception {

        CreateAccountDTO request = new CreateAccountDTO(BigDecimal.valueOf(3000));

        when(accountService.createAccount(request))
                .thenThrow(
                        new BadRequestException("Account already exists"));

        mockMvc.perform(post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Account already exists"));

        verify(accountService).createAccount(request);
    }

    @Test
    void itShouldReturn400WhenCreatingAccountWithoutInitialBalance()
            throws Exception {

        CreateAccountDTO request = new CreateAccountDTO(null);

        mockMvc.perform(post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(accountService, never())
                .createAccount(any(CreateAccountDTO.class));
    }

    @Test
    void itShouldUpdateAccount() throws Exception {
        UpdateAccountDTO request = new UpdateAccountDTO(BigDecimal.valueOf(4500));

        AccountResponseDTO response = new AccountResponseDTO(
                UUID.randomUUID(),
                BigDecimal.valueOf(4500),
                Instant.now(),
                Instant.now());

        when(accountService.updateAccount(request))
                .thenReturn(response);

        mockMvc.perform(put("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.initialBalance").value(4500));

        verify(accountService).updateAccount(request);
    }

    @Test
    void itShouldReturn404WhenUpdatingAccountDoesNotExist()
            throws Exception {

        UpdateAccountDTO request = new UpdateAccountDTO(BigDecimal.valueOf(4500));

        when(accountService.updateAccount(request))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Account was not initialized or found"));

        mockMvc.perform(put("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Account was not initialized or found"));

        verify(accountService).updateAccount(request);
    }
}