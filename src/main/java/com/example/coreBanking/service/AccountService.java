package com.example.coreBanking.service;

import com.example.coreBanking.dto.response.AccountResponse;
import com.example.coreBanking.dto.response.BalanceResponse;
import com.example.coreBanking.exception.*;
import com.example.coreBanking.model.Account;
import com.example.coreBanking.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final Map<String, String> documentToAccount = new ConcurrentHashMap<>();

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountResponse createAccount(String documentNumber) {
        if (documentToAccount.containsKey(documentNumber)) {
            throw new AccountAlreadyExistException("Document already has an account");
        }
        String accountId = UUID.randomUUID().toString();
        Account account = new Account(accountId, BigDecimal.ZERO);
        accountRepository.save(account);
        documentToAccount.put(documentNumber, accountId);
        return new AccountResponse(accountId, documentNumber);
    }

    public AccountResponse getAccount(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        String document = documentToAccount.entrySet().stream()
                .filter(e -> e.getValue().equals(accountId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse("UNKNOWN");
        return new AccountResponse(account.getId(), document);
    }

    public BalanceResponse getBalance(String accountId) {
        return accountRepository.findById(accountId)
                .map(account -> new BalanceResponse(account.getBalance()))
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    public void reset() {
        accountRepository.reset();
    }

    public void configOverdraftLimit(String accountId, BigDecimal limit) {
        Account account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException("Account not found"));
        account.setOverdraftLimit(limit);
        accountRepository.save(account);
    }

}
