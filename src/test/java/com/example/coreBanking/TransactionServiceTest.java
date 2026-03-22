package com.example.coreBanking;

import com.example.coreBanking.dto.request.TransactionRequest;
import com.example.coreBanking.dto.response.TransactionResponse;
import com.example.coreBanking.exception.AccountNotFoundException;
import com.example.coreBanking.model.Account;
import com.example.coreBanking.model.Transaction;
import com.example.coreBanking.repository.AccountRepository;
import com.example.coreBanking.repository.TransactionRepository;
import com.example.coreBanking.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

    @Test
    void shouldCreateDepositTransaction() {

        String accountId = "acc-1";
        Account account = new Account(accountId, BigDecimal.ZERO);

        TransactionRequest request = new TransactionRequest(accountId, 4, BigDecimal.valueOf(100));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        TransactionResponse response = transactionService.createTransaction(request);

        assertEquals(accountId, response.getAccountId());
        assertEquals(BigDecimal.valueOf(100), account.getBalance());

        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldCreateWithdrawUsingOverdraft() {

        String accountId = "acc-1";
        Account account = new Account(accountId, BigDecimal.valueOf(100));
        account.setOverdraftLimit(BigDecimal.valueOf(200));

        TransactionRequest request = new TransactionRequest(accountId, 1, BigDecimal.valueOf(250));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        transactionService.createTransaction(request);

        assertEquals(BigDecimal.valueOf(-150), account.getBalance());

        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldThrowWhenInsufficientFundsEvenWithOverdraft() {

        String accountId = "acc-1";
        Account account = new Account(accountId, BigDecimal.valueOf(100));
        account.setOverdraftLimit(BigDecimal.valueOf(50));

        TransactionRequest request = new TransactionRequest(accountId, 1, BigDecimal.valueOf(200));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createTransaction(request));
    }

    @Test
    void shouldThrowWhenAccountNotFound() {

        String accountId = "invalid";

        TransactionRequest request = new TransactionRequest(accountId, 4, BigDecimal.valueOf(100));

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> transactionService.createTransaction(request));
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

        List<TransactionResponse> response =
                transactionService.getTransactionByAccountId(accountId);

        assertEquals(2, response.size());
        assertEquals(accountId, response.get(0).getAccountId());
    }

    @Test
    void shouldThrowWhenAccountNotFoundOnHistory() {

        String accountId = "invalid";

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> transactionService.getTransactionByAccountId(accountId));
    }
}