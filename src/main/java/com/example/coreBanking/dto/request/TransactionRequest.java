package com.example.coreBanking.dto.request;

import java.math.BigDecimal;

public class TransactionRequest {
    private String accountId;
    private int operationTypeId;
    private BigDecimal amount;

    public TransactionRequest( String accountId, int operationTypeId, BigDecimal amount) {
        this.accountId = accountId;
        this.operationTypeId = operationTypeId;
        this.amount = amount;
    }

    public TransactionRequest() {

    }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public int getOperationTypeId() { return operationTypeId; }
    public void setOperationTypeId(int operationTypeId) { this.operationTypeId = operationTypeId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}