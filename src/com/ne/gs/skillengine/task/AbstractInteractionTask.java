/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.task;

import java.util.concurrent.Future;

import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public abstract class AbstractInteractionTask {

    protected Player requestor;
    protected VisibleObject responder;
    private Future<?> task;
    private int interval = 2500;

    /**
     * @param requestor
     * @param responder
     */
    public AbstractInteractionTask(Player requestor, VisibleObject responder) {
        // super();
        this.requestor = requestor;
        if (responder == null) {
            this.responder = requestor;
        } else {
            this.responder = responder;
        }
    }

    /**
     * Called on each interaction
     */
    protected abstract boolean onInteraction();

    /**
     * Called when interaction is complete
     */
    protected abstract void onInteractionFinish();

    /**
     * Called before interaction is started
     */
    protected abstract void onInteractionStart();

    /**
     * Called when interaction is not complete and need to be aborted
     */
    protected abstract void onInteractionAbort();

    /**
     * Interaction scheduling method
     */
    public void start() {
        synchronized (this) {
            onInteractionStart();

            task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

                @Override
                public void run() {
                    synchronized (AbstractInteractionTask.this) {
                        if (!validateParticipants()) {
                            stop(true);
                        }

                        boolean stopTask = onInteraction();
                        if (stopTask) {
                            stop(false);
                        }
                    }
                }

            }, 1000, interval);
        }
    }

    public void stop(boolean participantNull) {
        synchronized (this) {
            if (!participantNull) {
                onInteractionFinish();
            }
            if (task != null && !task.isCancelled()) {
                task.cancel(false);
                task = null;
            }
        }
    }

    /**
     * Abort current interaction
     */
    public void abort() {
        synchronized (this) {
            onInteractionAbort();
            stop(true);
        }
    }

    /**
     * @return true or false
     */
    public boolean isInProgress() {
        return task != null && !task.isCancelled();
    }

    /**
     * @return true or false
     */
    public boolean validateParticipants() {
        return requestor != null;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}
