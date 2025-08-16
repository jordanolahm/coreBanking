package com.example.coreBanking.dto.response;

import java.math.BigDecimal;

public class BalanceResponse {
    private BigDecimal balance;

    public BalanceResponse(BigDecimal balance) { this.balance = balance; }

    public void setBalance(BigDecimal nowBalance) {this.balance = nowBalance; }
    public BigDecimal getBalance() { return balance; }
}
