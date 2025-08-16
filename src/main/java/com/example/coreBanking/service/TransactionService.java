package com.example.coreBanking.service;

import com.example.coreBanking.dto.request.EventRequest;
import com.example.coreBanking.dto.request.TransactionRequest;
import com.example.coreBanking.dto.response.TransactionResponse;
import com.example.coreBanking.exception.*;
import com.example.coreBanking.model.Account;
import com.example.coreBanking.model.Transaction;
import com.example.coreBanking.repository.AccountRepository;
import com.example.coreBanking.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    public TransactionResponse createTransaction(TransactionRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        BigDecimal amount = normalizeAmount(request.getOperationTypeId(), request.getAmount());
        BigDecimal available = account.getBalance().add(account.getOverdraftLimit());

        if (amount.compareTo(BigDecimal.ZERO) < 0 && available.add(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transaction");
        }

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transactionOnCreate = new Transaction(request.getAccountId(), request.getOperationTypeId(), amount);
        transactionRepository.save(transactionOnCreate);

        return new TransactionResponse(transactionOnCreate.getTransactionId(), transactionOnCreate.getAccountId(),
                transactionOnCreate.getOperationTypeId(), transactionOnCreate.getAmount(), transactionOnCreate.getEventDate());
    }

    public List<TransactionResponse> getTransactionsToday() {
        LocalDateTime today = LocalDateTime.now();
        List<Transaction> transactions = transactionRepository.findAllTransactionOnDateTime(today);
        return redirectTransactionResponse(transactions);
    }

    public List<TransactionResponse> getTransactionsInRange(LocalDateTime begin, LocalDateTime end) {
        if (begin == null || end == null) {
            throw new DateTimeFormatException("Begin or end date is invalid. Verify format date.");
        }

        List<Transaction> transactions = transactionRepository.findAllTransactionsBetweenDate(begin, end);
        return redirectTransactionResponse(transactions);
    }

    public List<TransactionResponse> getTransactionsByType(int operationTypeId) {
        if (operationTypeId < 1 || operationTypeId > 4) {
            throw new OperationTypeDoesntExistException("Operation type doesn't exist");
        }

        List<Transaction> transactions = transactionRepository.findAllOperationTypeById(operationTypeId);
        List<TransactionResponse> response = new ArrayList<>();

        for (Transaction t : transactions) {
            response.add(new TransactionResponse(
                    t.getTransactionId(),
                    t.getAccountId(),
                    t.getOperationTypeId(),
                    t.getAmount(),
                    t.getEventDate()
            ));
        }
        return response;
    }

    public TransactionResponse getTransactionById(long transactionId) {
        Transaction idFoundedTransaction = transactionRepository.findTransactionById(transactionId);
        if (idFoundedTransaction == null) {
            throw new TransactionNotFoundException("Transaction not found");
        }
        return new TransactionResponse(
                idFoundedTransaction.getTransactionId(),
                idFoundedTransaction.getAccountId(),
                idFoundedTransaction.getOperationTypeId(),
                idFoundedTransaction.getAmount(),
                idFoundedTransaction.getEventDate()
        );
    }

    public Object handleTransaction(EventRequest request) {
        return switch (request.getType()) {
            case "deposit" -> handleDeposit(request);
            case "withdraw" -> handleWithdraw(request);
            case "transfer" -> handleTransfer(request);
            default -> throw new IllegalArgumentException("Invalid event type");
        };
    }

    private Object handleDeposit(EventRequest request) {
        Account account = accountRepository.findById(request.getDestination())
                .orElseGet(() -> new Account(request.getDestination(), BigDecimal.ZERO));
        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);
        return Map.of("destination", account);
    }

    private Object handleWithdraw(EventRequest request) {
        Account account = accountRepository.findById(request.getOrigin())
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        BigDecimal available = account.getBalance().add(account.getOverdraftLimit());
        if (available.compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds, including overdraft");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);
        return Map.of("origin", account);
    }

    private Object handleTransfer(EventRequest request) {
        Account origin = accountRepository.findById(request.getOrigin())
                .orElseThrow(() -> new AccountNotFoundException("Origin account not found"));
        Account destination = accountRepository.findById(request.getDestination())
                .orElseGet(() -> new Account(request.getDestination(), BigDecimal.ZERO));

        BigDecimal available = origin.getBalance().add(origin.getOverdraftLimit());
        if (available.compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds, including overdraft");
        }

        origin.setBalance(origin.getBalance().subtract(request.getAmount()));
        destination.setBalance(destination.getBalance().add(request.getAmount()));

        accountRepository.save(origin);
        accountRepository.save(destination);

        return Map.of("origin", origin, "destination", destination);
    }

    private BigDecimal normalizeAmount(int operationTypeId, BigDecimal amount) {
        return switch (operationTypeId) {
            case 1, 2, 3 -> amount.negate();
            case 4 -> amount;
            default -> throw new OperationTypeDoesntExistException("Operation type doesn't exist");
        };
    }

    private List<TransactionResponse> redirectTransactionResponse(List<Transaction> transactions) {
        List<TransactionResponse> resultSearchList = new ArrayList<>();
        for (Transaction t : transactions) {
            resultSearchList.add(new TransactionResponse(
                    t.getTransactionId(),
                    t.getAccountId(),
                    t.getOperationTypeId(),
                    t.getAmount(),
                    t.getEventDate()
            ));
        }
        return resultSearchList;
    }
}
