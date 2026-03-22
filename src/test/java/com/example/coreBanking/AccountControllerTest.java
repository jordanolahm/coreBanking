package com.example.coreBanking;

import com.example.coreBanking.controller.AccountController;
import com.example.coreBanking.dto.request.AccountRequest;
import com.example.coreBanking.dto.request.OverdraftRequest;
import com.example.coreBanking.dto.response.AccountResponse;
import com.example.coreBanking.dto.response.BalanceResponse;
import com.example.coreBanking.service.AccountService;
import com.example.coreBanking.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import com.example.coreBanking.dto.response.TransactionResponse;

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

    private AccountResponse accountResponse;
    private BalanceResponse balanceResponse;

    @BeforeEach
    void setUp() {
        accountResponse = new AccountResponse("1234", "12345678900");
        balanceResponse = new BalanceResponse(new BigDecimal("500.00"));
    }

    @Test
    void createAccount_shouldReturnCreatedAccount() throws Exception {
        AccountRequest request = new AccountRequest();
        request.setDocumentNumber("12345678900");

        Mockito.when(accountService.createAccount(eq("12345678900"))).thenReturn(accountResponse);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value("1234"))
                .andExpect(jsonPath("$.documentNumber").value("12345678900"));
    }

    @Test
    void getAccount_shouldReturnAccount() throws Exception {
        Mockito.when(accountService.getAccount("1234")).thenReturn(accountResponse);

        mockMvc.perform(get("/api/accounts/1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("1234"))
                .andExpect(jsonPath("$.documentNumber").value("12345678900"));
    }

    @Test
    void getBalance_shouldReturnBalance() throws Exception {
        Mockito.when(accountService.getBalance("1234")).thenReturn(balanceResponse);

        mockMvc.perform(get("/api/accounts/balance")
                        .param("account_id", "1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.00));
    }

    @Test
    void setOverdraft_shouldReturnOk() throws Exception {
        OverdraftRequest request = new OverdraftRequest();
        request.setAccountId("1234");
        request.setLimit(new BigDecimal("100.00"));

        mockMvc.perform(post("/api/accounts/overdraft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void reset_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/accounts/reset"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnTransactionsForAccount() throws Exception {

        String accountId = "abc-123";

        List<TransactionResponse> mockResponse = List.of(
                new TransactionResponse(1L, accountId, 4, BigDecimal.valueOf(200), LocalDateTime.now()),
                new TransactionResponse(2L, accountId, 1, BigDecimal.valueOf(-100), LocalDateTime.now())
        );

        when(transactionService.getTransactionByAccountId(accountId))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/accounts/{accountId}/transactions", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].accountId").value(accountId))
                .andExpect(jsonPath("$[0].operationTypeId").value(4))
                .andExpect(jsonPath("$[0].amount").value(200))
                .andExpect(jsonPath("$[1].operationTypeId").value(1))
                .andExpect(jsonPath("$[1].amount").value(-100));
    }
}