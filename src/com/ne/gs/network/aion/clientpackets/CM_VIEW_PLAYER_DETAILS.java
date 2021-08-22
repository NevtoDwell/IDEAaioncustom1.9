/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.configs.administration.AdminConfig;
import com.ne.gs.model.gameobjects.VisibleObject;
import com.ne.gs.model.gameobjects.player.DeniedStatus;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.ne.gs.network.aion.serverpackets.SM_VIEW_PLAYER_DETAILS;

/**
 * @author Avol
 */
public class CM_VIEW_PLAYER_DETAILS extends AionClientPacket {

    private static final Logger log = LoggerFactory.getLogger(CM_VIEW_PLAYER_DETAILS.class);

    private int targetObjectId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        targetObjectId = readD();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        VisibleObject obj = player.getKnownList().getObject(targetObjectId);
        if (obj == null) {
            // probably targetObjectId can be 0
            log.warn("CHECKPOINT: can't show player details for " + targetObjectId);
            return;
        }

        if (obj instanceof Player) {
            Player target = (Player) obj;

            if (!target.getPlayerSettings().isInDeniedStatus(DeniedStatus.VIEW_DETAILS) || player.getAccessLevel() >= AdminConfig.ADMIN_VIEW_DETAILS) {
                sendPacket(new SM_VIEW_PLAYER_DETAILS(target.getEquipment().getEquippedItemsWithoutStigma(), target));
            } else {
                sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_REJECTED_WATCH(target.getName()));
                return;
            }
        }
    }
}
