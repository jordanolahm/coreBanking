package com.example.coreBanking;

import com.example.coreBanking.controller.TransactionController;
import com.example.coreBanking.dto.request.EventRequest;
import com.example.coreBanking.dto.request.TransactionRequest;
import com.example.coreBanking.dto.response.TransactionResponse;
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
import java.util.Collections;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    private TransactionResponse sampleTransaction;

    @BeforeEach
    void setUp() {
        sampleTransaction = new TransactionResponse(
                1L,
                "account-123",
                4,
                BigDecimal.valueOf(100),
                LocalDateTime.now()
        );
    }

    @Test
    void testCreateTransaction() throws Exception {
        TransactionRequest request =  new TransactionRequest();
        request.setAccountId("account-123");
        request.setOperationTypeId(4);
        request.setAmount(BigDecimal.valueOf(100));

        Mockito.when(transactionService.createTransaction(any(TransactionRequest.class)))
                .thenReturn(sampleTransaction);

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value("account-123"))
                .andExpect(jsonPath("$.amount").value(100));
    }

    @Test
    void testHandleTransactionEvent() throws Exception {
        EventRequest request = new EventRequest();
        request.setType("deposit");
        request.setDestination("account-123");
        request.setAmount(BigDecimal.valueOf(100));

        Mockito.when(transactionService.handleTransaction(any(EventRequest.class)))
                .thenReturn(Collections.singletonMap("destination", sampleTransaction));

        mockMvc.perform(post("/api/transactions/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.destination.accountId").value("account-123"));
    }

    @Test
    void testGetTransactionById() throws Exception {
        Mockito.when(transactionService.getTransactionById(1L))
                .thenReturn(sampleTransaction);

        mockMvc.perform(get("/api/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(1));
    }

    @Test
    void testGetTransactionsToday() throws Exception {
        Mockito.when(transactionService.getTransactionsToday())
                .thenReturn(Collections.singletonList(sampleTransaction));

        mockMvc.perform(get("/api/transactions/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value("account-123"));
    }

    @Test
    void testGetTransactionsInRange() throws Exception {
        String begin = "2025-08-16T00:00:00";
        String end = "2025-08-16T23:59:59";

        Mockito.when(transactionService.getTransactionsInRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(sampleTransaction));

        mockMvc.perform(get("/api/transactions/range")
                        .param("begin", begin)
                        .param("end", end))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value("account-123"));
    }

    @Test
    void testGetTransactionsByType() throws Exception {
        Mockito.when(transactionService.getTransactionsByType(4))
                .thenReturn(Collections.singletonList(sampleTransaction));

        mockMvc.perform(get("/api/transactions/type/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].operationTypeId").value(4));
    }
}