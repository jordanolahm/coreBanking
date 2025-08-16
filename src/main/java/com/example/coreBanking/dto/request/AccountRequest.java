package com.example.coreBanking.dto.request;

public class AccountRequest {
    private String documentNumber;

    public AccountRequest() {}

    public AccountRequest(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }
}