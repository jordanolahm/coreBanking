package com.example.coreBanking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {
    private long transactionId;
    private String accountId;
    private int operationTypeId;
    private BigDecimal amount;
    private LocalDateTime eventDate;

    public TransactionResponse(long transactionId, String accountId, int operationTypeId, BigDecimal amount, LocalDateTime eventDate) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.operationTypeId = operationTypeId;
        this.amount = amount;
        this.eventDate = eventDate;
    }

    public long getTransactionId() { return transactionId; }
    public String getAccountId() { return accountId; }
    public int getOperationTypeId() { return operationTypeId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getEventDate() { return eventDate; }
}