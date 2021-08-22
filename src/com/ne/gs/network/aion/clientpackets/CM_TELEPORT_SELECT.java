/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import org.slf4j.LoggerFactory;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.TeleportAnimation;
import com.ne.gs.model.gameobjects.AionObject;
import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.teleport.TeleporterTemplate;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.utils.MathUtil;

/**
 * @author ATracer, orz, KID
 */
public class CM_TELEPORT_SELECT extends AionClientPacket {

    /**
     * NPC ID
     */
    public int targetObjectId;

    /**
     * Destination of teleport
     */
    public int locId;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readImpl() {
        // empty
        targetObjectId = readD();
        locId = readD(); // locationId
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (player.getLifeStats().isAlreadyDead()) {
            return;
        }

        AionObject obj = player.getKnownList().getObject(targetObjectId);
        if (obj != null && obj instanceof Npc) {
            Npc npc = (Npc) obj;
            int npcId = npc.getNpcId();
            if (!MathUtil.isInRange(npc, player, npc.getObjectTemplate().getTalkDistance() + 2)) {
                return;
            }
            TeleporterTemplate teleport = DataManager.TELEPORTER_DATA.getTeleporterTemplateByNpcId(npcId);
            if (teleport != null) {
                TeleportService.teleport(teleport, locId, player, npc, TeleportAnimation.JUMP_AIMATION);
            } else {
                LoggerFactory.getLogger(CM_TELEPORT_SELECT.class).warn("teleportation id " + locId + " was not found on npc " + npcId);
            }
        } else {
            LoggerFactory.getLogger(CM_TELEPORT_SELECT.class).debug(
                "player " + player.getName() + " requested npc " + targetObjectId + " for teleportation " + locId
                    + ", but he doesnt have such npc in knownlist");
        }
    }
}
