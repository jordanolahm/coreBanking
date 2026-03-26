package com.example.coreBanking;

import com.example.coreBanking.dto.response.AccountResponse;
import com.example.coreBanking.dto.response.BalanceResponse;
import com.example.coreBanking.exception.AccountAlreadyExistException;
import com.example.coreBanking.exception.AccountNotFoundException;
import com.example.coreBanking.model.Account;
import com.example.coreBanking.repository.AccountRepository;
import com.example.coreBanking.repository.TransactionRepository;
import com.example.coreBanking.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldCreateAccountSuccessfully() {

        String document = "123456789";

        AccountResponse response = accountService.createAccount(document);

        assertNotNull(response.getAccountId());
        assertEquals(document, response.getDocumentNumber());

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldThrowWhenDocumentIsInvalid() {

        assertThrows(IllegalArgumentException.class,
                () -> accountService.createAccount(null));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.createAccount(""));
    }

    @Test
    void shouldThrowWhenAccountAlreadyExists() {

        String document = "123";

        accountService.createAccount(document);

        assertThrows(AccountAlreadyExistException.class,
                () -> accountService.createAccount(document));
    }

    @Test
    void shouldGetAccountSuccessfully() {

        String accountId = "acc-1";
        String document = "123";

        Account account = new Account(accountId, BigDecimal.ZERO);

        accountService.createAccount(document);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccount(accountId);

        assertEquals(accountId, response.getAccountId());
    }

    @Test
    void shouldThrowWhenAccountNotFound() {

        when(accountRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.getAccount("invalid"));
    }

    @Test
    void shouldReturnBalanceSuccessfully() {

        String accountId = "acc-1";

        Account account = new Account(accountId, BigDecimal.valueOf(200));

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        BalanceResponse response = accountService.getBalance(accountId);

        assertEquals(BigDecimal.valueOf(200), response.getBalance());
    }

    @Test
    void shouldThrowWhenBalanceAccountNotFound() {

        when(accountRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.getBalance("invalid"));
    }

    @Test
    void shouldSetOverdraftLimitSuccessfully() {

        String accountId = "acc-1";

        Account account = new Account(accountId, BigDecimal.ZERO);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        accountService.configOverdraftLimit(accountId, BigDecimal.valueOf(500));

        assertEquals(BigDecimal.valueOf(500), account.getOverdraftLimit());

        verify(accountRepository).save(account);
    }

    @Test
    void shouldThrowWhenOverdraftInvalid() {

        assertThrows(IllegalArgumentException.class,
                () -> accountService.configOverdraftLimit("acc-1", null));

        assertThrows(IllegalArgumentException.class,
                () -> accountService.configOverdraftLimit("acc-1", BigDecimal.valueOf(-10)));
    }

    @Test
    void shouldThrowWhenSettingOverdraftForNonexistentAccount() {

        when(accountRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.configOverdraftLimit("invalid", BigDecimal.valueOf(100)));
    }

    @Test
    void shouldResetSystem() {

        accountService.reset();

        verify(accountRepository).reset();
        verify(transactionRepository).reset();
    }
}