/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.actions.PlayerMode;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.state.CreatureState;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.world.World;

/**
 * Packet about player flying teleport movement.
 *
 * @author -Nemesiss-, Sweetkr, KID
 */
public class CM_MOVE_IN_AIR extends AionClientPacket {

    float x, y, z;
    int distance;
    @SuppressWarnings("unused")
    private byte locationId;
    @SuppressWarnings("unused")
    private int worldId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        worldId = readD();
        x = readF();
        y = readF();
        z = readF();
        locationId = (byte) readC();
        distance = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();

        if (player.isInState(CreatureState.FLIGHT_TELEPORT)) {
            if (player.isUsingFlyTeleport()) {
                player.setFlightDistance(distance);
            } else if (player.isInPlayerMode(PlayerMode.WINDSTREAM_STARTED)) {
                player.windstreamPath.distance = distance;
            }
            World.getInstance().updatePosition(player, x, y, z, (byte) 0);
            player.getMoveController().updateLastMove();
        }
    }
}
