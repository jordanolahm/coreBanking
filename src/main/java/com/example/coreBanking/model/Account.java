package com.example.coreBanking.model;
import java.math.BigDecimal;

public class Account {

    private String id;
    private BigDecimal balance;
    private BigDecimal overdraftLimit;

    public Account(String id, BigDecimal balance) {
        this.id = id;
        this.balance = balance;
        this.overdraftLimit = BigDecimal.ZERO;
    }

    public String getId() { return id; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public BigDecimal getOverdraftLimit() {return overdraftLimit; }
    public void setOverdraftLimit(BigDecimal newLimit) { this.overdraftLimit = newLimit; }
}
