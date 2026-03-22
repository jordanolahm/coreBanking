package com.example.coreBanking.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.math.BigDecimal;

public class EventRequest {
    public enum EventType {
        DEPOSIT,
        WITHDRAW,
        TRANSFER;

        @JsonCreator
        public static EventType from(String value) {
            return EventType.valueOf(value.toUpperCase());
        }
    }

    private EventType type;
    private String origin;
    private String destination;
    private BigDecimal amount;

    public EventRequest(EventType type, String origin, String destination, BigDecimal amount) {
        this.type = type;
        this.origin = origin;
        this.destination = destination;
        this.amount = amount;
    }

    public EventRequest() {}

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
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
}


