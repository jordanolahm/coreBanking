package com.example.coreBanking.repository;

import com.example.coreBanking.model.Transaction;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class TransactionRepository {

    private final List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());

    public Transaction save(Transaction transaction) {
        transactions.add(transaction);
        return transaction;
    }

    public Optional<Transaction> findTransactionById(Long transactionId) {
        synchronized (transactions) {
            return transactions.stream()
                    .filter(t -> t.getTransactionId() == transactionId)
                    .findFirst();
        }
    }

    public List<Transaction> findAllOperationTypeById( int operationTypeId) {
        List<Transaction> resultFindProcess = new ArrayList<>();
        for( Transaction t: transactions) {
            if( t.getOperationTypeId() == operationTypeId) {
                resultFindProcess.add(t);
            }
        }
        return resultFindProcess;
    }

    public List<Transaction> findAllTransactionsBetweenDate(LocalDateTime begin, LocalDateTime end) {
        List<Transaction> listTransactionRangeDate = new ArrayList<>();

        for (Transaction t : transactions) {
            LocalDateTime transactionDate = t.getEventDate();

            if ((transactionDate.isEqual(begin) || transactionDate.isAfter(begin)) &&
                    (transactionDate.isEqual(end) || transactionDate.isBefore(end))) {
                listTransactionRangeDate.add(t);
            }
        }

        return listTransactionRangeDate;
    }

    public List<Transaction> findAllTransactionOnDateTime(LocalDateTime date) {
        List<Transaction> listTransactionsInDate = new ArrayList<>();
        for(Transaction t : transactions) {
            LocalDateTime transactionDate = t.getEventDate();
            if (transactionDate.toLocalDate().equals(date.toLocalDate())) {
                listTransactionsInDate.add(t);
            }
        }

        return listTransactionsInDate;
    }

    public List<Transaction> findByAccountId(String accountId) {
        synchronized (transactions) {
            return transactions.stream()
                    .filter(t -> t.getAccountId().equals(accountId))
                    .toList();
        }
    }

    public List<Transaction> findAll() {
        synchronized (transactions) {
            return new ArrayList<>(transactions);
        }
    }

    public void reset() {
        synchronized (transactions) {
            transactions.clear();
        }
    }
}
