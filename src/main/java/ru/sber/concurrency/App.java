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
    public static void main(String[] args) {
        for (int i = 3; i < 15; ++i) {
            System.out.println(format("%s\t\t\t%s",
                    test(new CounterBarrier(i), i).toMillis(),
                    test(new ArrayBarrier(i), i).toMillis()
                    ));
        }
    }

    private static Duration test(Barrier barrier, int n) {
        var executor = Executors.newFixedThreadPool(n);
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
            Thread.sleep(100 + random.nextInt(1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
