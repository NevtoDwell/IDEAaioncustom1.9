/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.Gatherable;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;

/**
 * @author ATracer
 */
public class CM_GATHER extends AionClientPacket {

    boolean isStartGather = false;

    @Override
    protected void readImpl() {
        int action = readD();
        if (action == 0) {
            isStartGather = true;
        }
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        VisibleObject target = player.getTarget();
        if (target != null && target.getPosition().isSpawned() && target instanceof Gatherable) {
            if (isStartGather) {
                ((Gatherable) target).getController().onStartUse(player);
            } else {
                player.getController().cancelGathering();
                //((Gatherable) target).getController().finishGathering(player);
            }
        }
    }
}
