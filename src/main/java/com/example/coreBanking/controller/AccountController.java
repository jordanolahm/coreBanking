package com.example.coreBanking.controller;

import com.example.coreBanking.dto.request.AccountRequest;
import com.example.coreBanking.dto.response.AccountResponse;
import com.example.coreBanking.dto.response.BalanceResponse;
import com.example.coreBanking.dto.request.OverdraftRequest;
import com.example.coreBanking.dto.response.TransactionResponse;
import com.example.coreBanking.service.AccountService;
import com.example.coreBanking.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @Autowired
    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountId) {
        return ResponseEntity.ok(accountService.getAccount(accountId));
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(@RequestParam("account_id") String accountId) {
        return ResponseEntity.ok(accountService.getBalance(accountId));
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<?> getAccountTransactions(@PathVariable String accountId) {

        List<TransactionResponse> transactions =
                transactionService.getTransactionByAccountId(accountId);

        if (transactions.isEmpty()) {
            return ResponseEntity.ok("No transactions found");
        }

        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(request.getDocumentNumber()));
    }

    @PostMapping("/overdraft")
    public ResponseEntity<?> setOverdraft(@RequestBody OverdraftRequest request) {
        accountService.configOverdraftLimit(request.getAccountId(), request.getLimit());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<String> reset() {
        accountService.reset();
        return ResponseEntity.ok("System has been reset successfully");
    }
}
