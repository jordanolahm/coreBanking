package com.example.coreBanking.dto.response;

public class EventResponse {

    private AccountResponse origin;
    private AccountResponse destination;

    public EventResponse(AccountResponse origin, AccountResponse destination) {
        this.origin = origin;
        this.destination = destination;
    }

    public AccountResponse getOrigin() { return origin; }
    public AccountResponse getDestination() { return destination; }
}