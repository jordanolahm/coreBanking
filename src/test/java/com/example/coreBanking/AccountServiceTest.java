package com.example.coreBanking;

import com.example.coreBanking.dto.BalanceResponse;
import com.example.coreBanking.dto.EventRequest;
import com.example.coreBanking.exception.AccountNotFoundException;
import com.example.coreBanking.exception.InsufficientFundsException;
import com.example.coreBanking.model.Account;
import com.example.coreBanking.repository.AccountRepository;
import com.example.coreBanking.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    private AccountRepository accountRepository;
    private AccountService accountService;

    @BeforeEach
    void setup() {
        accountRepository = mock(AccountRepository.class);
        accountService = new AccountService(accountRepository);
    }

    @Test
    void testGetBalance_Success() {
        Account account = new Account("123", BigDecimal.valueOf(1000));
        when(accountRepository.findById("123")).thenReturn(Optional.of(account));

        BalanceResponse response = accountService.getBalance("123");
        assertEquals(BigDecimal.valueOf(1000), response.getBalance());
        verify(accountRepository).findById("123");
    }

    @Test
    void testGetBalance_AccountNotFound() {
        when(accountRepository.findById("123")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> accountService.getBalance("123"));
    }

    @Test
    void testConfigOverdraftLimit_Success() {
        Account account = new Account("123", BigDecimal.ZERO);
        when(accountRepository.findById("123")).thenReturn(Optional.of(account));

        accountService.configOverdraftLimit("123", BigDecimal.valueOf(500));
        assertEquals(BigDecimal.valueOf(500), account.getOverdraftLimit());
        verify(accountRepository).save(account);
    }

    @Test
    void testConfigOverdraftLimit_AccountNotFound() {
        when(accountRepository.findById("123")).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class,
                () -> accountService.configOverdraftLimit("123", BigDecimal.valueOf(500)));
    }

    @Test
    void testHandleEvent_Deposit_NewAccount() {
        EventRequest request = new EventRequest("deposit", null, "123", BigDecimal.valueOf(200));
        when(accountRepository.findById("123")).thenReturn(Optional.empty());

        Map<String, Object> response = (Map<String, Object>) accountService.handleEvent(request);

        verify(accountRepository).save(any(Account.class));
        assertTrue(response.containsKey("destination"));
        Account acc = (Account) response.get("destination");
        assertEquals("123", acc.getId());
        assertEquals(BigDecimal.valueOf(200), acc.getBalance());
    }

    @Test
    void testHandleEvent_Withdraw_Success() {
        Account account = new Account("123", BigDecimal.valueOf(300));
        account.setOverdraftLimit(BigDecimal.valueOf(100));
        when(accountRepository.findById("123")).thenReturn(Optional.of(account));

        EventRequest request = new EventRequest("withdraw", "123", null, BigDecimal.valueOf(350));
        Map<String, Object> response = (Map<String, Object>) accountService.handleEvent(request);

        verify(accountRepository).save(account);
        assertEquals(BigDecimal.valueOf(-50), account.getBalance());
        assertTrue(response.containsKey("origin"));
    }

    @Test
    void testHandleEvent_Withdraw_InsufficientFunds() {
        Account account = new Account("123", BigDecimal.valueOf(200));
        account.setOverdraftLimit(BigDecimal.valueOf(50));
        when(accountRepository.findById("123")).thenReturn(Optional.of(account));

        EventRequest request = new EventRequest("withdraw", "123", null, BigDecimal.valueOf(300));
        assertThrows(InsufficientFundsException.class, () -> accountService.handleEvent(request));
    }

    @Test
    void testHandleEvent_Transfer_Success() {
        Account origin = new Account("123", BigDecimal.valueOf(400));
        origin.setOverdraftLimit(BigDecimal.valueOf(100));
        Account destination = new Account("456", BigDecimal.valueOf(100));
        when(accountRepository.findById("123")).thenReturn(Optional.of(origin));
        when(accountRepository.findById("456")).thenReturn(Optional.of(destination));

        EventRequest request = new EventRequest("transfer", "123", "456", BigDecimal.valueOf(450));
        Map<String, Object> response = (Map<String, Object>) accountService.handleEvent(request);

        verify(accountRepository).save(origin);
        verify(accountRepository).save(destination);

        assertEquals(BigDecimal.valueOf(-50), origin.getBalance());
        assertEquals(BigDecimal.valueOf(550), destination.getBalance());

        assertTrue(response.containsKey("origin"));
        assertTrue(response.containsKey("destination"));
    }

    @Test
    void testHandleEvent_Transfer_InsufficientFunds() {
        Account origin = new Account("123", BigDecimal.valueOf(300));
        origin.setOverdraftLimit(BigDecimal.valueOf(50));
        when(accountRepository.findById("123")).thenReturn(Optional.of(origin));
        when(accountRepository.findById("456")).thenReturn(Optional.empty()); // Destination may be new, but let's test with existing for clarity

        EventRequest request = new EventRequest("transfer", "123", "456", BigDecimal.valueOf(400));
        assertThrows(InsufficientFundsException.class, () -> accountService.handleEvent(request));
    }

    @Test
    void testReset() {
        doNothing().when(accountRepository).reset();
        accountService.reset();
        verify(accountRepository).reset();
    }
}