package com.example.coreBanking.dto.response;

public class AccountResponse {
    private String accountId;
    private String documentNumber;

    public AccountResponse(String accountId, String documentNumber) {
        this.accountId = accountId;
        this.documentNumber = documentNumber;
    }

    public String getAccountId() { return accountId; }
    public String getDocumentNumber() { return documentNumber; }
}