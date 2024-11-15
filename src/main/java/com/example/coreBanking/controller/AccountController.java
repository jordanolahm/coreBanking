package com.example.coreBanking.controller;

import com.example.coreBanking.dto.BalanceResponse;
import com.example.coreBanking.dto.EventRequest;
import com.example.coreBanking.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }


    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(@RequestParam("account_id") String accountId) {
        return ResponseEntity.ok(accountService.getBalance(accountId));
    }

    @PostMapping("/event")
    public ResponseEntity<?> handleEvent(@RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.handleEvent(request));
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        accountService.reset();
        return ResponseEntity.ok().build();
    }
}
