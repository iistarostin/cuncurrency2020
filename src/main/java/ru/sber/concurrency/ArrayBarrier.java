package ru.sber.concurrency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.unmodifiableList;

public class ArrayBarrier implements Barrier {
    private final int limit;
    private final List<AtomicInteger> flags;

    public ArrayBarrier(int limit) {
        this.limit = limit;
        var temp = new ArrayList<AtomicInteger>(limit);
        for (int i = 0; i < limit; i++) {
            //shared cache possible but unlikely since object are allocated separately
             temp.add(new AtomicInteger(0));
        }
        flags = unmodifiableList(temp);
    }

    @Override
    public void pass(final int n) {
        while (n != 0 && flags.get(n - 1).get() != 1);
        flags.get(n).set(1);
        while(n != limit - 1 && flags.get(n + 1).get() != 2);
        flags.get(n).set(2);
    }
}
