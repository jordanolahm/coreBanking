package com.example.coreBanking;

import com.example.coreBanking.dto.request.EventRequest;
import com.example.coreBanking.dto.response.EventResponse;
import com.example.coreBanking.dto.response.TransactionResponse;
import com.example.coreBanking.exception.AccountNotFoundException;
import com.example.coreBanking.exception.InsufficientFundsException;
import com.example.coreBanking.manager.LockManager;
import com.example.coreBanking.model.Account;
import com.example.coreBanking.model.Transaction;
import com.example.coreBanking.repository.AccountRepository;
import com.example.coreBanking.repository.TransactionRepository;
import com.example.coreBanking.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Mock
    private LockManager lockManager;

    @BeforeEach
    void setup() {
        lenient().when(lockManager.getLock(anyString()))
                .thenReturn(new ReentrantLock());
    }

    @Test
    void shouldHandleDepositEvent() {
        String accountId = "acc-1";
        Account account = new Account(accountId, BigDecimal.ZERO);

        EventRequest request = new EventRequest();
        request.setType(EventRequest.EventType.DEPOSIT);
        request.setDestination(accountId);
        request.setAmount(BigDecimal.valueOf(100));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        EventResponse response = transactionService.handleTransaction(request);

        assertEquals(BigDecimal.valueOf(100), account.getBalance());
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldHandleWithdrawUsingOverdraft() {
        String accountId = "acc-1";
        Account account = new Account(accountId, BigDecimal.valueOf(100));
        account.setOverdraftLimit(BigDecimal.valueOf(200));

        EventRequest request = new EventRequest();
        request.setType(EventRequest.EventType.WITHDRAW);
        request.setOrigin(accountId);
        request.setAmount(BigDecimal.valueOf(250));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        transactionService.handleTransaction(request);

        assertEquals(BigDecimal.valueOf(-150), account.getBalance());
        verify(accountRepository).save(account);
    }

    @Test
    void shouldThrowWhenInsufficientFunds() {
        String accountId = "acc-1";
        Account account = new Account(accountId, BigDecimal.valueOf(100));
        account.setOverdraftLimit(BigDecimal.valueOf(50));

        EventRequest request = new EventRequest();
        request.setType(EventRequest.EventType.WITHDRAW);
        request.setOrigin(accountId);
        request.setAmount(BigDecimal.valueOf(200));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class,
                () -> transactionService.handleTransaction(request));
    }

    @Test
    void shouldThrowWhenAccountNotFound() {
        String accountId = "invalid";
        EventRequest request = new EventRequest();
        request.setType(EventRequest.EventType.DEPOSIT);
        request.setDestination(accountId);
        request.setAmount(BigDecimal.valueOf(100));

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> transactionService.handleTransaction(request));
    }

    @Test
    void shouldReturnTransactionsByAccountId() {
        String accountId = "acc-1";
        Account account = new Account(accountId, BigDecimal.ZERO);
        List<Transaction> transactions = List.of(
                new Transaction(accountId, 4, BigDecimal.valueOf(100)),
                new Transaction(accountId, 1, BigDecimal.valueOf(-50))
        );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountId(accountId)).thenReturn(transactions);

        List<TransactionResponse> response = transactionService.getTransactionByAccountId(accountId);

        assertEquals(2, response.size());
        verify(transactionRepository).findByAccountId(accountId);
    }

    @Test
    void shouldThrowWhenAccountNotFoundOnHistory() {
        String accountId = "invalid";
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> transactionService.getTransactionByAccountId(accountId));
    }
}