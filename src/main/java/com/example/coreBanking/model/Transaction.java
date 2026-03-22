package com.example.coreBanking.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private static long counter = 0;

    private long transactionId;
    private String accountId;
    private int operationTypeId;
    private BigDecimal amount;
    private LocalDateTime eventDate;

    public Transaction(String accountId, int operationTypeId, BigDecimal amount) {
        this.transactionId = counter++;
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
