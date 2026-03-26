package com.example.coreBanking.service;

import com.example.coreBanking.dto.request.EventRequest;
import com.example.coreBanking.dto.response.AccountResponse;
import com.example.coreBanking.dto.response.EventResponse;
import com.example.coreBanking.dto.response.TransactionResponse;
import com.example.coreBanking.exception.*;
import com.example.coreBanking.manager.LockManager;
import com.example.coreBanking.model.Account;
import com.example.coreBanking.model.Transaction;
import com.example.coreBanking.repository.AccountRepository;
import com.example.coreBanking.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final LockManager lockManager;
    private final Set<String> countBlocker = Set.of("123", "1234", "12345");


    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository, LockManager lockManager) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.lockManager = lockManager;
    }
    //123 -> Eventrequest -> request.getOrigin() -> "123"
    public EventResponse handleTransaction(EventRequest request) {
   
        if (request.getType() == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }
      /*      
        1 - Construimos a lista estatica das contas <string, Account>; 
        2 - Validar se a conta dada pelo EventRequest request.getOrigin() é igual ao countBlocker(); 
        3 - Se a conta estiver na lista blocker, handleTransaction lanca exception. 
        4 - Se nao tiver, ele procede com a operacao.. 
        */
        
        if(request.getOrigin() != countBlocker.contains(request.getOrigin())) {
              throw new Exception("Origin account is blocker");         
        }

        if(request.getDestination() != countBlocker.contains(request.getDestination())) {
            throw new Exception("Destination account is blocker");
        }

        return switch (request.getType()) {
            case DEPOSIT -> deposit(request);
            case WITHDRAW -> withdraw(request);
            case TRANSFER -> transfer(request);
        };
    }

    public TransactionResponse getTransactionById(long transactionId) {

        Transaction transaction = transactionRepository.findTransactionById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getAccountId(),
                transaction.getOperationTypeId(),
                transaction.getAmount(),
                transaction.getEventDate()
        );
    }

    public List<TransactionResponse> getTransactionsToday() {

        LocalDateTime now = LocalDateTime.now();

        List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(t -> t.getEventDate().toLocalDate().equals(now.toLocalDate()))
                .toList();

        return transactions.stream()
                .map(t -> new TransactionResponse(
                        t.getTransactionId(),
                        t.getAccountId(),
                        t.getOperationTypeId(),
                        t.getAmount(),
                        t.getEventDate()
                ))
                .toList();
    }

    public List<TransactionResponse> getTransactionByAccountId(String accountId) {

        accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        return transactions.stream()
                .map(t -> new TransactionResponse(
                        t.getTransactionId(),
                        t.getAccountId(),
                        t.getOperationTypeId(),
                        t.getAmount(),
                        t.getEventDate()
                ))
                .toList();
    }

    private EventResponse deposit(EventRequest request) {

        ReentrantLock lock = lockManager.getLock(request.getDestination());
        lock.lock();

        try {
            Account destination = accountRepository.findById(request.getDestination())
                    .orElseThrow(() -> new AccountNotFoundException("Account not found"));

            destination.deposit(request.getAmount());

            accountRepository.save(destination);
            transactionRepository.save(new Transaction(
                    destination.getId(), 4, request.getAmount()
            ));

            return new EventResponse(
                    null,
                    new AccountResponse(destination.getId(), null)
            );
        } finally {
            lock.unlock();
        }
    }

    private EventResponse withdraw(EventRequest request) {

        ReentrantLock lock = lockManager.getLock(request.getOrigin());
        lock.lock();

        try {
            Account origin = accountRepository.findById(request.getOrigin())
                    .orElseThrow(() -> new AccountNotFoundException("Account not found"));

            origin.withdraw(request.getAmount());

            accountRepository.save(origin);
            transactionRepository.save(new Transaction(
                    origin.getId(), 1, request.getAmount().negate()
            ));

            return new EventResponse(
                    new AccountResponse(origin.getId(), null),
                    null
            );
        } finally {
            lock.unlock();
        }
    }

    private EventResponse transfer(EventRequest request) {

        String originId = request.getOrigin();
        String destinationId = request.getDestination();

        if (originId == null || destinationId == null) {
            throw new IllegalArgumentException("Origin and destination are required");
        }

        if (originId.equals(destinationId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }


        ReentrantLock lock1 = lockManager.getLock(originId);
        ReentrantLock lock2 = lockManager.getLock(destinationId);

        List<ReentrantLock> locks = Stream.of(lock1, lock2)
                .sorted(Comparator.comparingInt(System::identityHashCode))
                .toList();

        locks.get(0).lock();
        locks.get(1).lock();

        try {
            Account origin = accountRepository.findById(request.getOrigin())
                    .orElseThrow(() -> new AccountNotFoundException("Origin account not found"));

            Account destination = accountRepository.findById(request.getDestination())
                    .orElseThrow(() -> new AccountNotFoundException("Destination account not found"));

            origin.withdraw(request.getAmount());
            destination.deposit(request.getAmount());

            accountRepository.save(origin);
            accountRepository.save(destination);

            transactionRepository.save(new Transaction(origin.getId(), 1, request.getAmount().negate()));
            transactionRepository.save(new Transaction(destination.getId(), 4, request.getAmount()));

            return new EventResponse(
                    new AccountResponse(origin.getId(), null),
                    new AccountResponse(destination.getId(), null)
            );
        } finally {
            locks.get(1).unlock();
            locks.get(0).unlock();
        }

    }
}
