/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.skillengine.task;

import com.ne.commons.utils.Rnd;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;

/**
 * @author ATracer, synchro2
 */
public abstract class AbstractCraftTask extends AbstractInteractionTask {

    protected int completeValue = 100;
    protected int currentSuccessValue;
    protected int currentFailureValue;
    protected int skillLvlDiff;

    public AbstractCraftTask(Player requestor, VisibleObject responder, int skillLvlDiff) {
        super(requestor, responder);
        this.skillLvlDiff = skillLvlDiff;
    }

    @Override
    protected boolean onInteraction() {
        if (currentSuccessValue == completeValue) {
            return onSuccessFinish();
        }
        if (currentFailureValue == completeValue) {
            onFailureFinish();
            return true;
        }

        analyzeInteraction();

        sendInteractionUpdate();
        return false;
    }

    /**
     * Perform interaction calculation
     */
    protected void analyzeInteraction() {
        // TODO better random
        // if(Rnd.nextBoolean())
        int multi = Math.max(0, 33 - skillLvlDiff * 5);
        if (Rnd.get(100) > multi) {
            currentSuccessValue += Rnd.get(completeValue / (multi + 1) / 2, completeValue);
        } else {
            currentFailureValue += Rnd.get(completeValue / (multi + 1) / 2, completeValue);
        }

        if (currentSuccessValue >= completeValue) {
            currentSuccessValue = completeValue;
        } else if (currentFailureValue >= completeValue) {
            currentFailureValue = completeValue;
        }
    }

    protected abstract void sendInteractionUpdate();

    protected abstract boolean onSuccessFinish();

    protected abstract void onFailureFinish();
}
