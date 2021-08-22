/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.taskmanager;

import java.util.concurrent.locks.ReentrantLock;

import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author NB4L1 Going to remove this - Nemesiss
 */
public abstract class FIFOExecutableQueue implements Runnable {

    private static final byte NONE = 0;
    private static final byte QUEUED = 1;
    private static final byte RUNNING = 2;

    private final ReentrantLock lock = new ReentrantLock();

    private volatile byte state = NONE;

    protected final void execute() {
        lock();
        try {
            if (state != NONE) {
                return;
            }

            state = QUEUED;
        } finally {
            unlock();
        }

        ThreadPoolManager.getInstance().execute(this);
    }

    public final void lock() {
        lock.lock();
    }

    public final void unlock() {
        lock.unlock();
    }

    @Override
    public final void run() {
        try {
            while (!isEmpty()) {
                setState(QUEUED, RUNNING);

                try {
                    while (!isEmpty()) {
                        removeAndExecuteFirst();
                    }
                } finally {
                    setState(RUNNING, QUEUED);
                }
            }
        } finally {
            setState(QUEUED, NONE);
        }
    }

    private void setState(byte expected, byte value) {
        lock();
        try {
            if (state != expected) {
                throw new IllegalStateException("state: " + state + ", expected: " + expected);
            }
        } finally {
            state = value;

            unlock();
        }
    }

    protected abstract boolean isEmpty();

    protected abstract void removeAndExecuteFirst();
}
