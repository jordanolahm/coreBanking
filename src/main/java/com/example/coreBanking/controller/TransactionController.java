package com.example.coreBanking.controller;

import com.example.coreBanking.dto.request.EventRequest;
import com.example.coreBanking.dto.response.EventResponse;
import com.example.coreBanking.dto.response.TransactionResponse;
import com.example.coreBanking.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/event")
    public ResponseEntity<EventResponse> handleTransaction(@RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.handleTransaction(request));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable long transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    @GetMapping("/today")
    public ResponseEntity<List<TransactionResponse>> getTransactionsToday() {
        return ResponseEntity.ok(transactionService.getTransactionsToday());
    }
}
