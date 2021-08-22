/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.clientpackets;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.TeleportAnimation;
import com.ne.gs.model.gameobjects.Creature;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.RequestResponseHandler;
import com.ne.gs.model.templates.spawns.SpawnSearchResult;
import com.ne.gs.network.aion.AionClientPacket;
import com.ne.gs.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.ne.gs.network.aion.serverpackets.SM_SHOW_NPC_ON_MAP;
import com.ne.gs.services.teleport.TeleportService;
import com.ne.gs.utils.PacketSendUtility;

/**
 * @author Lyahim
 */
public class CM_OBJECT_SEARCH extends AionClientPacket {

    private int npcId;

    /**
     * Nothing to do
     */
    @Override
    protected void readImpl() {
        npcId = readD();
    }

    /**
     * Logging
     */
    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        final SpawnSearchResult searchResult = DataManager.SPAWNS_DATA2.getFirstSpawnByNpcId(0, npcId);
        if (searchResult != null) {
            sendPacket(new SM_SHOW_NPC_ON_MAP(npcId, searchResult.getWorldId(), searchResult.getSpot().getX(),
                    searchResult.getSpot().getY(), searchResult.getSpot().getZ()));
            if (player.isGM()) {
                PacketSendUtility.sendMessage(player, "NpcID: " + npcId);
                RequestResponseHandler responseHandler = new RequestResponseHandler(player) {
                    @Override
                    public void acceptRequest(Creature p2, Player p) {
                        TeleportService.teleportTo(p, searchResult.getWorldId(), searchResult.getSpot().getX(),
                                searchResult.getSpot().getY(), searchResult.getSpot().getZ(), (byte) 0);
                    }

                    @Override
                    public void denyRequest(Creature p2, Player p) {
                    }
                };
                boolean requested = player.getResponseRequester().putRequest(902247, responseHandler);
                if (requested) {
                    PacketSendUtility.sendPck(player, new SM_QUESTION_WINDOW(902247, 0, 0, "Совершить телепорт к NpcID: " + npcId + " ?"));
                }
            }
        }
    }
}
