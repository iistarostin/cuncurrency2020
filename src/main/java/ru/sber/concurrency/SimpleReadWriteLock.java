package ru.sber.concurrency;

import lombok.Getter;

public class SimpleReadWriteLock {
    private int readers;
    private boolean writer;

    public interface Lock {
        void lock();
        void unlock();
    }

    @Getter
    private final Lock readLock;
    @Getter
    private final Lock writeLock;

    public SimpleReadWriteLock() {
        writer = false;
        readers = 0;

        readLock = new Lock() {
            @Override
            public void lock() {
                synchronized (SimpleReadWriteLock.this) {
                    while (writer) {
                        try {
                            SimpleReadWriteLock.this.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    ++readers;
                }
            }

            @Override
            public void unlock() {
                synchronized (SimpleReadWriteLock.this) {
                    --readers;
                    if (readers == 0) SimpleReadWriteLock.this.notifyAll();
                }
            }
        };

        writeLock = new Lock() {
            @Override
            public void lock() {
                synchronized (SimpleReadWriteLock.this) {
                    while (readers > 0 || writer) {
                        try {
                            SimpleReadWriteLock.this.wait();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    writer = true;
                }
            }

            @Override
            public void unlock() {
                synchronized (SimpleReadWriteLock.this) {
                    writer = false;
                    SimpleReadWriteLock.this.notifyAll();
                }
            }
        };
    }
}
