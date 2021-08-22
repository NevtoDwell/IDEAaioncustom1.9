/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ne.gs.model.gameobjects.Npc;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.templates.teleport.TeleporterTemplate;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.world.World;

/**
 * @author alexa026 , orz
 */
public class SM_TELEPORT_MAP extends AionServerPacket {

    private final int targetObjectId;
    private final Player player;
    private final TeleporterTemplate teleport;
    public Npc npc;

    private static final Logger log = LoggerFactory.getLogger(SM_TELEPORT_MAP.class);

    public SM_TELEPORT_MAP(Player player, int targetObjectId, TeleporterTemplate teleport) {
        this.player = player;
        this.targetObjectId = targetObjectId;
        npc = (Npc) World.getInstance().findVisibleObject(targetObjectId);
        this.teleport = teleport;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        if (teleport != null && teleport.getTeleportId() != 0) {
            writeD(targetObjectId);
            writeH(teleport.getTeleportId());
        } else {
            player.sendMsg("Missing info at npc_teleporter.xml with npcid: " + npc.getNpcId());
            log.info(String.format("Missing teleport info with npcid: %d", npc.getNpcId()));
        }
    }
}
