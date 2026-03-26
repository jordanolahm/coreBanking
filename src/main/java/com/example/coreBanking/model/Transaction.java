package com.example.coreBanking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class Transaction {

    private static final AtomicLong counter = new AtomicLong();
    private long transactionId;
    private String accountId;
    private int operationTypeId;
    private BigDecimal amount;
    private LocalDateTime eventDate;

    public Transaction(String accountId, int operationTypeId, BigDecimal amount) {
        this.transactionId = counter.incrementAndGet();
        this.accountId = accountId;
        this.operationTypeId = operationTypeId;
        this.amount = amount;
        this.eventDate = LocalDateTime.now();
    }

    public long getTransactionId() { return transactionId; }
    public String getAccountId() { return accountId; }
    public int getOperationTypeId() { return operationTypeId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getEventDate() { return eventDate; }
}
