package com.example.coreBanking.dto;

import java.math.BigDecimal;

public class EventRequest {
    private String type;
    private String origin;
    private String destination;
    private BigDecimal amount;

    public String getType() {
        return type;
    }

    public String getOrigin() {
        return origin;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDestination() {
        return destination;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public void setType(String type) {
        this.type = type;
    }
}


