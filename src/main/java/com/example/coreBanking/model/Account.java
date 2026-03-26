package com.example.coreBanking.model;
import com.example.coreBanking.exception.InsufficientFundsException;

import java.math.BigDecimal;

public class Account {

    private String id;
    private BigDecimal balance;
    private BigDecimal overdraftLimit;

    public Account(String id, BigDecimal initialBalance) {
        this.id = id;
        this.balance = initialBalance != null ? initialBalance : BigDecimal.ZERO;
        this.overdraftLimit = BigDecimal.ZERO;
    }

    public String getId() { return id; }
    public BigDecimal getBalance() { return balance; }
    public BigDecimal getOverdraftLimit() {return overdraftLimit; }
    public void setOverdraftLimit(BigDecimal newLimit) {
        if (newLimit == null || newLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Invalid overdraft limit");
        }

        if (balance.compareTo(newLimit.negate()) < 0) {
            throw new IllegalArgumentException("New limit is lower than current overdraft usage");
        }

        this.overdraftLimit = newLimit;
    }

    public BigDecimal getAvailableBalance() {
        return balance.add(overdraftLimit);
    }
    public boolean isActiveOverdraft() {
        return balance.compareTo(BigDecimal.ZERO) < 0;
    }

    public synchronized void withdraw(BigDecimal amount) {
        validateAmount(amount);

        if (amount.compareTo(getAvailableBalance()) > 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        balance = balance.subtract(amount);
    }

    public synchronized void deposit(BigDecimal amount) {
        validateAmount(amount);
        balance = balance.add(amount);
    }


    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
    }
}
