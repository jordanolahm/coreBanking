package com.example.coreBanking.dto.request;

import java.math.BigDecimal;

public class OverdraftRequest {
    private String accountId;
    private BigDecimal limit;

    public OverdraftRequest(String accountId, BigDecimal limit) {
        this.accountId = accountId;
        this.limit = limit;
    }

    public OverdraftRequest() {

    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setLimit(BigDecimal limit) {
        this.limit = limit;
    }

    public BigDecimal getLimit() {
        return limit;
    }
}
