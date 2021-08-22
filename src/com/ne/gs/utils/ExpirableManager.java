/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.utils;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ScheduledFuture;
import com.google.common.primitives.Longs;
import org.slf4j.LoggerFactory;

import com.ne.commons.Sys;
import com.ne.commons.network.util.ThreadPoolManager;

/**
 * @author hex1r0
 */
public class ExpirableManager implements Runnable {

    private final PriorityQueue<Expirable> _expirables = new PriorityQueue<>(10, new Comparator<Expirable>() {
        @Override
        public int compare(Expirable e1, Expirable e2) {
            return Longs.compare(e1.expiresAt(), e2.expiresAt());
        }
    });

    private ScheduledFuture<?> _future = null;
    private int _taskCount = 0; // just for info purpose - no need to be volatile

    public void put(Expirable e) {
        synchronized (_expirables) {
            if (_expirables.contains(e)) {
                return;
            }

            if (_expirables.isEmpty()) {
                add(e);
                schedule();
            } else if (e.expiresAt() >= head().expiresAt()) {
                add(e);
            } else {
                boolean schedule = false;
                if (_future != null) {
                    schedule = _future.cancel(false);
                }

                add(e);

                if (schedule) {
                    schedule();
                }
            }
        }
    }

    public void take(Expirable e) {
        synchronized (_expirables) {
            if (_expirables.isEmpty()) {
                return;
            }

            Expirable head = head();
            remove(e);

            // if we removed head we need to schelule next task after cancelation
            if (e.equals(head)) {
                if (_future != null) {
                   boolean res = _future.cancel(false);
                   if (!res) { // will be removed later if everything is OK
                       LoggerFactory.getLogger(ExpirableManager.class).error("ExpirableManager: res == false");
                   }
                   if (res && !_expirables.isEmpty()) {
                       schedule();
                   }
                }
            }
        }
    }

    private void add(Expirable e) {
        _expirables.add(e);
    }

	private void remove(Expirable e) {
        _expirables.remove(e);
	}

    private Expirable head() {
        return _expirables.peek();
    }

    private void schedule() {
        long expireAt = Math.max(0L, head().expiresAt() - Sys.millis());

        _future = ThreadPoolManager.getInstance().schedule(this, expireAt);
    }

    @Override
    public void run() {
        synchronized (_expirables) {
            Expirable head = _expirables.poll();

            head.expire();

            if (!_expirables.isEmpty()) {
                schedule();
            } else {
                _future = null;
            }

            _taskCount++;
        }
    }

    public int getTaskCount() {
        return _taskCount;
    }

    public interface Expirable {

        long expiresAt();

        void expire();
    }

    //    interface FiringExpirable extends Expirable {
    //        long fireDelay();
    //    }
}
