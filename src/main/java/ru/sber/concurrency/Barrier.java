package ru.sber.concurrency;

public interface Barrier {
    void pass(final int n);
}
