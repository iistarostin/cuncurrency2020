package ru.sber.concurrency;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

import static java.lang.String.format;
import static java.time.Instant.now;

public class App {
    private static final Random random = new Random();
    public static void main(String[] args) throws InterruptedException {
        testBarriers();
        testAccounts();
    }

    static void testAccounts() throws InterruptedException {
        final Account a1 = new Account(0);
        final Account a2 = new Account(0);
        var threads = List.of(
                new Thread(() -> {
                    a1.transfer(100, a2);
                }),
                new Thread(() -> {
                    a2.transfer(100, a1);
                }));
        threads.forEach(Thread::start);
        Thread.sleep(1000);
        a1.deposit(1000);
        a2.deposit(1000);
        for (Thread thread : threads) {
            thread.join();
        }
    }

    static void testBarriers() {
        System.out.println("N\t\tCounter\t\tArray");
        for (int i = 3; i < 20; ++i) {
            System.out.println(format("%d\t\t%s\t\t\t%s",
                    i,
                    test(new CounterBarrier(i), i).toMillis(),
                    test(new ArrayBarrier(i), i).toMillis()
            ));
        }
    }

    private static Duration test(Barrier barrier, int n) {
        List<Thread> threads = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            final int thread_number = i;
            threads.add(new Thread(() -> {
                foo();
                barrier.pass(thread_number);
            }));
        }
        return measureRuntime(() -> {
            threads.forEach(Thread::start);
            threads.forEach(thread -> {
                try {
                    thread.join();
                } catch (InterruptedException e) {}
            });
        });

    }

    private static Duration measureRuntime(Runnable runnable) {
        var start = now();
        runnable.run();
        return Duration.between(start, now());
    }

    private static void foo() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
