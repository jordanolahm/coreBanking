package com.example.coreBanking.repository;

import com.example.coreBanking.model.Transaction;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TransactionRepository {

    private final List<Transaction> transactions = new ArrayList<>();

    public Transaction save(Transaction transaction) {
        transactions.add(transaction);
        return transaction;
    }

    public Transaction findTransactionById(Long transactionId) {
        for(Transaction t : transactions) {
            if(t.getTransactionId() == transactionId) {
                return t;
            }
        }
        return null;
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
            //problems in compare with atributes year, month, day when comparing LocalDateTime
            if (transactionDate.toLocalDate().equals(date.toLocalDate())) {
                listTransactionsInDate.add(t);
            }
        }

        return listTransactionsInDate;
    }

    public List<Transaction> findAll() {
        return transactions;
    }

    public void reset() {
        transactions.clear();
    }
}
