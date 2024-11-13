package com.example.coreBanking.service;

import com.example.coreBanking.dto.BalanceResponse;
import com.example.coreBanking.dto.EventRequest;
import com.example.coreBanking.exception.AccountNotFoundException;
import com.example.coreBanking.exception.InsufficientFundsException;
import com.example.coreBanking.model.Account;
import com.example.coreBanking.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public BalanceResponse getBalance(String accountId) {
        return accountRepository.findById(accountId)
                .map(account -> new BalanceResponse(account.getBalance()))
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
    }

    public Object handleEvent(EventRequest request) {
        return switch (request.getType()) {
            case "deposit" -> handleDeposit(request);
            case "withdraw" -> handleWithdraw(request);
            case "transfer" -> handleTransfer(request);
            default -> throw new IllegalArgumentException("Invalid event type");
        };
    }

    private Object handleDeposit(EventRequest request) {
        Account account = accountRepository.findById(request.getDestination())
                .orElseGet(() -> new Account(request.getDestination(), BigDecimal.ZERO));
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);
        return Map.of("destination", account);
    }

    private Object handleWithdraw(EventRequest request) {
        Account account = accountRepository.findById(request.getOrigin())
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);
        return Map.of("origin", account);
    }

    private Object handleTransfer(EventRequest request) {
        Account origin = accountRepository.findById(request.getOrigin())
                .orElseThrow(() -> new AccountNotFoundException("Origin account not found"));
        Account destination = accountRepository.findById(request.getDestination())
                .orElseGet(() -> new Account(request.getDestination(), BigDecimal.ZERO));

        if (origin.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        origin.setBalance(origin.getBalance().subtract(request.getAmount()));
        destination.setBalance(destination.getBalance().add(request.getAmount()));

        accountRepository.save(origin);
        accountRepository.save(destination);

        return Map.of("origin", origin, "destination", destination);
    }

    public void reset() {
        accountRepository.reset();
    }
}
