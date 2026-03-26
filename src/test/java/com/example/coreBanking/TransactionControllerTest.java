package com.example.coreBanking;

import com.example.coreBanking.controller.TransactionController;
import com.example.coreBanking.dto.request.EventRequest;
import com.example.coreBanking.dto.response.EventResponse;
import com.example.coreBanking.dto.response.TransactionResponse;
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
import java.time.LocalDateTime;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldHandleDepositEvent() throws Exception {

        EventRequest request = new EventRequest();
        request.setType(EventRequest.EventType.DEPOSIT);
        request.setDestination("acc-1");
        request.setAmount(BigDecimal.valueOf(100));

        EventResponse response = new EventResponse(null, null);

        Mockito.when(transactionService.handleTransaction(Mockito.any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/transactions/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGetTransactionById() throws Exception {

        long transactionId = 1L;

        TransactionResponse response = new TransactionResponse(
                transactionId,
                "acc-1",
                4,
                BigDecimal.valueOf(100),
                LocalDateTime.now()
        );

        Mockito.when(transactionService.getTransactionById(transactionId))
                .thenReturn(response);

        mockMvc.perform(get("/api/transactions/{id}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.accountId").value("acc-1"))
                .andExpect(jsonPath("$.amount").value(100));
    }

    @Test
    void shouldGetTransactionsToday() throws Exception {

        List<TransactionResponse> list = List.of(
                new TransactionResponse(
                        1L,
                        "acc-1",
                        4,
                        BigDecimal.valueOf(100),
                        LocalDateTime.now()
                )
        );

        Mockito.when(transactionService.getTransactionsToday())
                .thenReturn(list);

        mockMvc.perform(get("/api/transactions/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountId").value("acc-1"))
                .andExpect(jsonPath("$[0].amount").value(100));
    }
}