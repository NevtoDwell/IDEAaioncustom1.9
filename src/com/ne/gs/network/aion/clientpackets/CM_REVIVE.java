/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.ReviveType;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.player.PlayerReviveService;

/**
 * @author ATracer, orz, avol, Simple
 */
public class CM_REVIVE extends AionClientPacket {

    private int reviveId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        reviveId = readC();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player activePlayer = getConnection().getActivePlayer();

        if (!activePlayer.getLifeStats().isAlreadyDead()) {
            return;
        }

        ReviveType reviveType = ReviveType.getReviveTypeById(reviveId);

        switch (reviveType) {
            case BIND_REVIVE:
                PlayerReviveService.bindRevive(activePlayer);
                break;
            case REBIRTH_REVIVE:
                PlayerReviveService.rebirthRevive(activePlayer);
                break;
            case ITEM_SELF_REVIVE:
                PlayerReviveService.itemSelfRevive(activePlayer);
                break;
            case SKILL_REVIVE:
                PlayerReviveService.skillRevive(activePlayer);
                break;
            case KISK_REVIVE:
                PlayerReviveService.kiskRevive(activePlayer);
                break;
            case INSTANCE_REVIVE:
                PlayerReviveService.instanceRevive(activePlayer);
                break;
            default:
                break;
        }

    }
}
