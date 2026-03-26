package com.example.coreBanking;

import com.example.coreBanking.manager.LockManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

class LockManagerTest {

    private LockManager lockManager;

    @BeforeEach
    void setUp() {
        lockManager = new LockManager();
    }

    @Test
    @DisplayName("Deve retornar a mesma instância de Lock para o mesmo ID de conta")
    void shouldReturnSameLockForSameAccountId() {
        String accountId = "acc-123";

        ReentrantLock firstCall = lockManager.getLock(accountId);
        ReentrantLock secondCall = lockManager.getLock(accountId);

        assertNotNull(firstCall, "O lock não deve ser nulo");
        assertSame(firstCall, secondCall, "Devem ser exatamente a mesma instância de ReentrantLock");
    }

    @Test
    @DisplayName("Deve retornar instâncias diferentes para IDs de conta diferentes")
    void shouldReturnDifferentLocksForDifferentAccountIds() {
        String accountId1 = "acc-1";
        String accountId2 = "acc-2";

        ReentrantLock lock1 = lockManager.getLock(accountId1);
        ReentrantLock lock2 = lockManager.getLock(accountId2);

        assertNotSame(lock1, lock2, "Contas diferentes devem ter instâncias de trava independentes");
    }

    @Test
    @DisplayName("Deve garantir a mesma instância mesmo sob múltiplas chamadas concorrentes")
    void shouldBeThreadSafeWhenCreatingLocks() throws ExecutionException, InterruptedException {
        String accountId = "acc-concurrent";
        int numberOfThreads = 100;
        List<CompletableFuture<ReentrantLock>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> lockManager.getLock(accountId)));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        ReentrantLock primaryLock = futures.get(0).get();
        for (CompletableFuture<ReentrantLock> future : futures) {
            assertSame(primaryLock, future.get(), "Mesmo sob estresse, todos devem receber a mesma instância");
        }
    }
}
