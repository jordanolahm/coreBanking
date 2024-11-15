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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetBalance_Success() {
        String accountId = "12345";
        BigDecimal balance = BigDecimal.valueOf(1000);
        Account account = new Account(accountId, balance);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        BalanceResponse response = accountService.getBalance(accountId);

        assertEquals(balance, response.getBalance());
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void testGetBalance_AccountNotFound() {
        String accountId = "12345";

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getBalance(accountId));
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void testHandleEvent_Deposit() {
        String destinationId = "12345";
        BigDecimal amount = BigDecimal.valueOf(500);
        Account account = new Account(destinationId, BigDecimal.ZERO);
        EventRequest request = new EventRequest("deposit", null, destinationId, amount);

        when(accountRepository.findById(destinationId)).thenReturn(Optional.of(account));

        Object result = accountService.handleEvent(request);

        assertTrue(result instanceof Map);
        assertEquals(account, ((Map<?, ?>) result).get("destination"));
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testHandleEvent_Withdraw_Success() {
        String originId = "12345";
        BigDecimal initialBalance = BigDecimal.valueOf(1000);
        BigDecimal withdrawAmount = BigDecimal.valueOf(500);
        Account account = new Account(originId, initialBalance);
        EventRequest request = new EventRequest("withdraw", originId, null, withdrawAmount);

        when(accountRepository.findById(originId)).thenReturn(Optional.of(account));

        Object result = accountService.handleEvent(request);

        assertTrue(result instanceof Map);
        assertEquals(account, ((Map<?, ?>) result).get("origin"));
        assertEquals(initialBalance.subtract(withdrawAmount), account.getBalance());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testHandleEvent_Withdraw_InsufficientFunds() {
        String originId = "12345";
        BigDecimal initialBalance = BigDecimal.valueOf(100);
        BigDecimal withdrawAmount = BigDecimal.valueOf(500);
        Account account = new Account(originId, initialBalance);
        EventRequest request = new EventRequest("withdraw", originId, null, withdrawAmount);

        when(accountRepository.findById(originId)).thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class, () -> accountService.handleEvent(request));
        verify(accountRepository, never()).save(account);
    }

    @Test
    void testHandleEvent_Transfer_Success() {
        String originId = "12345";
        String destinationId = "67890";
        BigDecimal initialBalance = BigDecimal.valueOf(1000);
        BigDecimal transferAmount = BigDecimal.valueOf(500);
        Account origin = new Account(originId, initialBalance);
        Account destination = new Account(destinationId, BigDecimal.ZERO);
        EventRequest request = new EventRequest("transfer", originId, destinationId, transferAmount);

        when(accountRepository.findById(originId)).thenReturn(Optional.of(origin));
        when(accountRepository.findById(destinationId)).thenReturn(Optional.of(destination));

        Object result = accountService.handleEvent(request);

        assertTrue(result instanceof Map);
        assertEquals(origin.getBalance(), initialBalance.subtract(transferAmount));
        assertEquals(destination.getBalance(), transferAmount);
        verify(accountRepository, times(1)).save(origin);
        verify(accountRepository, times(1)).save(destination);
    }

    @Test
    void testHandleEvent_Transfer_InsufficientFunds() {
        String originId = "12345";
        String destinationId = "67890";
        BigDecimal initialBalance = BigDecimal.valueOf(100);
        BigDecimal transferAmount = BigDecimal.valueOf(500);
        Account origin = new Account(originId, initialBalance);
        EventRequest request = new EventRequest("transfer", originId, destinationId, transferAmount);

        when(accountRepository.findById(originId)).thenReturn(Optional.of(origin));
        when(accountRepository.findById(destinationId)).thenReturn(Optional.of(new Account(destinationId, BigDecimal.ZERO)));

        assertThrows(InsufficientFundsException.class, () -> accountService.handleEvent(request));
        verify(accountRepository, never()).save(origin);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testHandleEvent_InvalidEventType() {
        EventRequest request = new EventRequest("invalid", "12345", "67890", BigDecimal.valueOf(100));

        assertThrows(IllegalArgumentException.class, () -> accountService.handleEvent(request));
    }

    @Test
    void testReset() {
        accountService.reset();
        verify(accountRepository, times(1)).reset();
    }
}