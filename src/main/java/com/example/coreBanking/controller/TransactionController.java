package com.example.coreBanking.controller;

import com.example.coreBanking.dto.request.EventRequest;
import com.example.coreBanking.dto.request.TransactionRequest;
import com.example.coreBanking.dto.response.TransactionResponse;
import com.example.coreBanking.exception.DateTimeFormatException;
import com.example.coreBanking.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(request));
    }

    @PostMapping("/event")
    public ResponseEntity<?> handleTransactionEvent(@RequestBody EventRequest request) {
        System.out.println(request.getType());
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

    @GetMapping("/range")
    public ResponseEntity<List<TransactionResponse>> getTransactionsInRange(
            @RequestParam("begin") String beginDateString,
            @RequestParam("end") String endDateString) {

        LocalDateTime begin;
        LocalDateTime end;
        try {
            begin = LocalDateTime.parse(beginDateString);
            end = LocalDateTime.parse(endDateString);
        } catch (Exception e) {
            throw new DateTimeFormatException("Invalid date format, expected LocalDateTime format.");
        }
        return ResponseEntity.ok(transactionService.getTransactionsInRange(begin, end));
    }

    @GetMapping("/type/{operationTypeId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByType(@PathVariable int operationTypeId) {
        return ResponseEntity.ok(transactionService.getTransactionsByType(operationTypeId));
    }
}
