package ru.sber.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ArrayBarrier implements Barrier {
    private final int limit;
    private final List<AtomicInteger> bits;

    public ArrayBarrier(int limit) {
        this.limit = limit;
        bits = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
             bits.add(new AtomicInteger(0));
        }
    }

    @Override
    public void pass(final int n) {
        while (n != 0 && bits.get(n - 1).get() != 1);
        bits.get(n).set(1);
        while(n != limit - 1 && bits.get(n + 1).get() != 2);
        bits.get(n).set(2);
    }
}
