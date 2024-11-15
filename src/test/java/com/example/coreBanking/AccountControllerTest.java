package com.example.coreBanking;

import com.example.coreBanking.controller.AccountController;
import com.example.coreBanking.dto.BalanceResponse;
import com.example.coreBanking.dto.EventRequest;
import com.example.coreBanking.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Test
    void testGetBalance_Success() throws Exception {
        String accountId = "12345";
        BigDecimal balance = BigDecimal.valueOf(1000);
        BalanceResponse balanceResponse = new BalanceResponse(balance);

        when(accountService.getBalance(accountId)).thenReturn(balanceResponse);

        mockMvc.perform(get("/api/balance")
                        .param("account_id", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(balance));

        verify(accountService, times(1)).getBalance(accountId);
    }

    @Test
    void testHandleEvent_Deposit() throws Exception {
        EventRequest request = new EventRequest("deposit", null, "12345", BigDecimal.valueOf(500));
        Map<String, Object> response = Map.of("destination", Map.of("id", "12345", "balance", 500));

        when(accountService.handleEvent(any(EventRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"deposit\", \"destination\": \"12345\", \"amount\": 500}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.destination.id").value("12345"))
                .andExpect(jsonPath("$.destination.balance").value(500));

        verify(accountService, times(1)).handleEvent(any(EventRequest.class));
    }

    @Test
    void testHandleEvent_Withdraw() throws Exception {
        EventRequest request = new EventRequest("withdraw", "12345", null, BigDecimal.valueOf(200));
        Map<String, Object> response = Map.of("origin", Map.of("id", "12345", "balance", 800));

        when(accountService.handleEvent(any(EventRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"withdraw\", \"origin\": \"12345\", \"amount\": 200}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.origin.id").value("12345"))
                .andExpect(jsonPath("$.origin.balance").value(800));

        verify(accountService, times(1)).handleEvent(any(EventRequest.class));
    }

    @Test
    void testHandleEvent_Transfer() throws Exception {
        EventRequest request = new EventRequest("transfer", "12345", "67890", BigDecimal.valueOf(300));
        Map<String, Object> response = Map.of(
                "origin", Map.of("id", "12345", "balance", 700),
                "destination", Map.of("id", "67890", "balance", 300)
        );

        when(accountService.handleEvent(any(EventRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/event")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\": \"transfer\", \"origin\": \"12345\", \"destination\": \"67890\", \"amount\": 300}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.origin.id").value("12345"))
                .andExpect(jsonPath("$.origin.balance").value(700))
                .andExpect(jsonPath("$.destination.id").value("67890"))
                .andExpect(jsonPath("$.destination.balance").value(300));

        verify(accountService, times(1)).handleEvent(any(EventRequest.class));
    }

    @Test
    void testReset_Success() throws Exception {
        doNothing().when(accountService).reset();

        mockMvc.perform(post("/api/reset"))
                .andExpect(status().isOk());

        verify(accountService, times(1)).reset();
    }
}