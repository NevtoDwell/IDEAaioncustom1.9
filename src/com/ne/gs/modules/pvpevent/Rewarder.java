/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.modules.pvpevent;

import java.util.List;

/**
 * @author hex1r0
 */
public abstract class Rewarder<T, E> implements Runnable {

    private final List<T> _rewards;
    private final List<E> _receivers;

    Rewarder(List<T> rewards, List<E> receivers) {
        _rewards = rewards;
        _receivers = receivers;
    }

    @Override
    public final void run() {
        int count = Math.min(_rewards.size(), _receivers.size());
        for (int i = 0; i < count; i++) {
            if (i >= _rewards.size()) {
                break;
            }
            if (i >= _receivers.size()) {
                break;
            }

            apply(_rewards.get(i), _receivers.get(i));
        }
    }

    public abstract void apply(T reward, E receiver);
}
