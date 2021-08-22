/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.observer;

/**
 * @author ATracer
 */
public class StartMovingListener extends ActionObserver {

    private boolean effectorMoved = false;

    public StartMovingListener() {
        super(ObserverType.MOVE);
    }

    /**
     * @return the effectorMoved
     */
    public boolean isEffectorMoved() {
        return effectorMoved;
    }

    @Override
    public void moved() {
        effectorMoved = true;
    }
}
