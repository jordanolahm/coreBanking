package com.example.coreBanking;

import com.example.coreBanking.controller.AccountController;
import com.example.coreBanking.dto.request.AccountRequest;
import com.example.coreBanking.dto.request.OverdraftRequest;
import com.example.coreBanking.dto.response.AccountResponse;
import com.example.coreBanking.dto.response.BalanceResponse;
import com.example.coreBanking.dto.response.TransactionResponse;
import com.example.coreBanking.service.AccountService;
import com.example.coreBanking.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetAccount() throws Exception {
        String accountId = "123";

        Mockito.when(accountService.getAccount(accountId))
                .thenReturn(new AccountResponse(accountId, "999999999"));

        mockMvc.perform(get("/api/accounts/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.documentNumber").value("999999999"));
    }

    @Test
    void shouldGetBalance() throws Exception {
        String accountId = "123";

        Mockito.when(accountService.getBalance(accountId))
                .thenReturn(new BalanceResponse(BigDecimal.valueOf(100)));

        mockMvc.perform(get("/api/accounts/{accountId}/balance", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100));
    }

    @Test
    void shouldGetTransactionsByAccount() throws Exception {
        String accountId = "123";

        List<TransactionResponse> transactions = List.of(
                new TransactionResponse(1L, accountId, 4, BigDecimal.valueOf(100), null)
        );

        Mockito.when(transactionService.getTransactionByAccountId(accountId))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/accounts/{accountId}/transactions", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value(accountId))
                .andExpect(jsonPath("$[0].amount").value(100));
    }

    @Test
    void shouldCreateAccount() throws Exception {
        AccountRequest request = new AccountRequest();
        request.setDocumentNumber("999999999");

        Mockito.when(accountService.createAccount("999999999"))
                .thenReturn(new AccountResponse("123", "999999999"));

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value("123"))
                .andExpect(jsonPath("$.documentNumber").value("999999999"));
    }

    @Test
    void shouldSetOverdraftLimit() throws Exception {
        OverdraftRequest request = new OverdraftRequest();
        request.setAccountId("123");
        request.setLimit(BigDecimal.valueOf(500));

        mockMvc.perform(post("/api/accounts/overdraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(accountService)
                .configOverdraftLimit("123", BigDecimal.valueOf(500));
    }

    @Test
    void shouldResetSystem() throws Exception {
        mockMvc.perform(post("/api/accounts/reset"))
                .andExpect(status().isOk());

        Mockito.verify(accountService).reset();
    }
}