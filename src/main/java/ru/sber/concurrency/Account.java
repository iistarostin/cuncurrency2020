package ru.sber.concurrency;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Account {
    private final Lock lock;
    private final Condition condition;
    private int balance;
    private int preferredCount = 0;

    public Account(int balance) {
        this.balance = balance;
        lock = new ReentrantLock();
        condition = lock.newCondition();
    }

    void deposit(int k) {
        lock.lock();
        try {
            balance += k;
            condition.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    void withdraw(int k) {
        lock.lock();
        try {
            while (balance < k || preferredCount > 0) {
                try {
                    condition.await();
                } catch (InterruptedException e) { }
            }
            balance -= k;
        }
        finally {
            lock.unlock();
        }
    }

    void withdrawPreferred(int k) {
        lock.lock();
        ++preferredCount;
        try {
            while (balance < k) {
                try {
                    condition.await();
                } catch (InterruptedException e) { }
            }
            balance -= k;
        }
        finally {
            --preferredCount;
            condition.signalAll();
            lock.unlock();
        }
    }

    void transfer(int k, Account reserve) {
        lock.lock();
        try {
            reserve.withdraw(k);
            deposit(k);
        } finally {
            lock.unlock();
        }

    }
}
