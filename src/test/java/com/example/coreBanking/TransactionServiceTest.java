package com.example.coreBanking;

import com.example.coreBanking.dto.request.EventRequest;
import com.example.coreBanking.dto.request.TransactionRequest;
import com.example.coreBanking.dto.response.TransactionResponse;
import com.example.coreBanking.exception.AccountNotFoundException;
import com.example.coreBanking.exception.InsufficientFundsException;
import com.example.coreBanking.model.Account;
import com.example.coreBanking.model.Transaction;
import com.example.coreBanking.repository.AccountRepository;
import com.example.coreBanking.repository.TransactionRepository;
import com.example.coreBanking.service.TransactionService;
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

public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTransaction_SuccessDeposit() {
        TransactionRequest request = new TransactionRequest("acc1", 4, BigDecimal.valueOf(100));
        Account account = new Account("acc1", BigDecimal.ZERO);

        when(accountRepository.findById("acc1")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.createTransaction(request);

        assertEquals("acc1", response.getAccountId());
        assertEquals(BigDecimal.valueOf(100), response.getAmount());
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testCreateTransaction_InsufficientFunds() {
        TransactionRequest request = new TransactionRequest("acc1", 1, BigDecimal.valueOf(50));
        Account account = new Account("acc1", BigDecimal.ZERO); // no balance, no overdraft

        when(accountRepository.findById("acc1")).thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void testCreateTransaction_AccountNotFound() {
        TransactionRequest request = new TransactionRequest("acc1", 4, BigDecimal.valueOf(100));

        when(accountRepository.findById("acc1")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> transactionService.createTransaction(request));
    }

    @Test
    void testHandleDeposit() {
        EventRequest request = new EventRequest("deposit", null, "acc1", BigDecimal.valueOf(200));

        when(accountRepository.findById("acc1")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Account> result = (Map<String, Account>) transactionService.handleTransaction(request);
        assertEquals("acc1", result.get("destination").getId());
        assertEquals(BigDecimal.valueOf(200), result.get("destination").getBalance());
    }

    @Test
    void testHandleWithdraw_Success() {
        EventRequest request = new EventRequest("withdraw", "acc1", null, BigDecimal.valueOf(50));
        Account account = new Account("acc1", BigDecimal.valueOf(100));

        when(accountRepository.findById("acc1")).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Account> result = (Map<String, Account>) transactionService.handleTransaction(request);
        assertEquals(BigDecimal.valueOf(50), result.get("origin").getBalance());
    }

    @Test
    void testHandleWithdraw_InsufficientFunds() {
        EventRequest request = new EventRequest("withdraw", "acc1", null, BigDecimal.valueOf(100));
        Account account = new Account("acc1", BigDecimal.valueOf(50));

        when(accountRepository.findById("acc1")).thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class, () -> transactionService.handleTransaction(request));
    }

    @Test
    void testHandleTransfer_Success() {
        EventRequest request = new EventRequest("transfer", "acc1", "acc2", BigDecimal.valueOf(50));
        Account origin = new Account("acc1", BigDecimal.valueOf(100));
        Account destination = new Account("acc2", BigDecimal.valueOf(20));

        when(accountRepository.findById("acc1")).thenReturn(Optional.of(origin));
        when(accountRepository.findById("acc2")).thenReturn(Optional.of(destination));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Account> result = (Map<String, Account>) transactionService.handleTransaction(request);
        assertEquals(BigDecimal.valueOf(50), result.get("origin").getBalance());
        assertEquals(BigDecimal.valueOf(70), result.get("destination").getBalance());
    }

    @Test
    void testHandleTransfer_InsufficientFunds() {
        EventRequest request = new EventRequest("transfer", "acc1", "acc2", BigDecimal.valueOf(100));
        Account origin = new Account("acc1", BigDecimal.valueOf(50));

        when(accountRepository.findById("acc1")).thenReturn(Optional.of(origin));

        assertThrows(InsufficientFundsException.class, () -> transactionService.handleTransaction(request));
    }
}