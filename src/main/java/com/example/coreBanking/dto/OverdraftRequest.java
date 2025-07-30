package com.example.coreBanking.dto;

import java.math.BigDecimal;

public class OverdraftRequest {
    private String accountId;
    private BigDecimal limit;

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
