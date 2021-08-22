/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.controllers.observer;

import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.utils.MathUtil;

public abstract class DialogObserver extends ActionObserver {

    private final Player responder;
    private final Creature requester;
    private final int maxDistance;

    public DialogObserver(Creature requester, Player responder, int maxDistance) {
        super(ObserverType.MOVE);
        this.responder = responder;
        this.requester = requester;
        this.maxDistance = maxDistance;
    }

    @Override
    public void moved() {
        if (!MathUtil.isIn3dRange(responder, requester, maxDistance)) {
            tooFar(requester, responder);
        }
    }

    public abstract void tooFar(Creature paramCreature, Player paramPlayer);
}
