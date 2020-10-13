package ru.sber.concurrency;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class CounterBarrier implements Barrier{
    private final AtomicInteger counter = new AtomicInteger();
    private final int limit;

    @Override
    public void pass(final int n) {
        counter.incrementAndGet();
        while(counter.get() < limit);
    }
}
