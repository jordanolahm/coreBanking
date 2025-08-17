package com.example.coreBanking;

import com.example.coreBanking.dto.response.AccountResponse;
import com.example.coreBanking.dto.response.BalanceResponse;
import com.example.coreBanking.exception.AccountAlreadyExistException;
import com.example.coreBanking.exception.AccountNotFoundException;
import com.example.coreBanking.model.Account;
import com.example.coreBanking.repository.AccountRepository;
import com.example.coreBanking.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
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
    void testCreateAccount_Success() {
        String documentNumber = "12345678900";

        AccountResponse response = accountService.createAccount(documentNumber);

        assertNotNull(response.getAccountId());
        assertEquals(documentNumber, response.getDocumentNumber());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testCreateAccount_AlreadyExists() {
        String documentNumber = "12345678900";

        accountService.createAccount(documentNumber);

        Exception exception = assertThrows(AccountAlreadyExistException.class, () -> {
            accountService.createAccount(documentNumber);
        });

        assertEquals("Document already has an account", exception.getMessage());
    }

    @Test
    void testGetAccount_Success() {
        String accountId = UUID.randomUUID().toString();
        Account account = new Account(accountId, BigDecimal.ZERO);
        accountService.createAccount("doc123"); // adiciona no map
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccount(accountId);
        assertEquals(accountId, response.getAccountId());
    }

    @Test
    void testGetAccount_NotFound() {
        String accountId = "non-existent";
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(accountId));
    }

    @Test
    void testGetBalance_Success() {
        String accountId = UUID.randomUUID().toString();
        Account account = new Account(accountId, BigDecimal.valueOf(100));
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        BalanceResponse response = accountService.getBalance(accountId);
        assertEquals(BigDecimal.valueOf(100), response.getBalance());
    }

    @Test
    void testGetBalance_NotFound() {
        String accountId = "non-existent";
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getBalance(accountId));
    }

    @Test
    void testReset() {
        accountService.reset();
        verify(accountRepository, times(1)).reset();
    }

    @Test
    void testConfigOverdraftLimit_Success() {
        String accountId = UUID.randomUUID().toString();
        Account account = new Account(accountId, BigDecimal.ZERO);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        BigDecimal limit = BigDecimal.valueOf(500);
        accountService.configOverdraftLimit(accountId, limit);

        assertEquals(limit, account.getOverdraftLimit());
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testConfigOverdraftLimit_AccountNotFound() {
        String accountId = "non-existent";
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        BigDecimal limit = BigDecimal.valueOf(500);
        assertThrows(AccountNotFoundException.class, () -> accountService.configOverdraftLimit(accountId, limit));
    }
}