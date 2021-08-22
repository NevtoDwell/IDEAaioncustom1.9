/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import javolution.util.FastMap.Entry;

import com.ne.gs.dataholders.DataManager;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PortalCooldownList;
import com.ne.gs.model.team2.TemporaryPlayerTeam;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author xavier
 */
public class SM_INSTANCE_INFO extends AionServerPacket {

    private final Player player;
    private final boolean isAnswer;
    private final int cooldownId;
    private final int worldId;
    private final TemporaryPlayerTeam<?> playerTeam;

    public SM_INSTANCE_INFO(Player player, boolean isAnswer, TemporaryPlayerTeam<?> playerTeam) {
        this.player = player;
        this.isAnswer = isAnswer;
        this.playerTeam = playerTeam;
        worldId = 0;
        cooldownId = 0;
    }

    public SM_INSTANCE_INFO(Player player, int instanceId) {
        this.player = player;
        isAnswer = false;
        playerTeam = null;
        worldId = instanceId;
        cooldownId = (DataManager.INSTANCE_COOLTIME_DATA.getInstanceCooltimeByWorldId(instanceId) != null ? DataManager.INSTANCE_COOLTIME_DATA
            .getInstanceCooltimeByWorldId(instanceId).getId() : 0);
    }

    @Override
    protected void writeImpl(AionConnection con) {
        boolean hasTeam = playerTeam != null;
        writeC(!isAnswer ? 2 : hasTeam ? 1 : 0);
        writeC(cooldownId);
        writeD(0);
        if (isAnswer) {
            if (hasTeam) {
                writeH(playerTeam.getMembers().size());
                for (Player p : playerTeam.getMembers()) {
                    PortalCooldownList cooldownList = p.getPortalCooldownList();
                    writeD(p.getObjectId());
                    writeH(cooldownList.size());
                    Entry<Integer, Long> e = cooldownList.getPortalCoolDowns().head();
                    for (Entry<Integer, Long> end = cooldownList.getPortalCoolDowns().tail(); (e = e.getNext()) != end; ) {
                        writeD(DataManager.INSTANCE_COOLTIME_DATA.getInstanceCooltimeByWorldId(e.getKey()).getId());
                        writeD(0);
                        writeD((int) (e.getValue() - System.currentTimeMillis()) / 1000);
                    }
                    writeS(p.getName());
                }
            } else {
                writeH(1);
                PortalCooldownList cooldownList = player.getPortalCooldownList();
                writeD(player.getObjectId());
                writeH(cooldownList.size());
                Entry<Integer, Long> e = cooldownList.getPortalCoolDowns().head();
                for (Entry<Integer, Long> end = cooldownList.getPortalCoolDowns().tail(); (e = e.getNext()) != end; ) {
                    writeD(DataManager.INSTANCE_COOLTIME_DATA.getInstanceCooltimeByWorldId(e.getKey()).getId());
                    writeD(0);
                    writeD((int) (e.getValue() - System.currentTimeMillis()) / 1000);
                }
                writeS(player.getName());
            }

        } else if (cooldownId == 0) {
            writeH(1);
            PortalCooldownList cooldownList = player.getPortalCooldownList();
            writeD(player.getObjectId());
            writeH(cooldownList.size());
            Entry<Integer, Long> e = cooldownList.getPortalCoolDowns().head();
            for (Entry<Integer, Long> end = cooldownList.getPortalCoolDowns().tail(); (e = e.getNext()) != end; ) {
                writeD(DataManager.INSTANCE_COOLTIME_DATA.getInstanceCooltimeByWorldId(e.getKey()).getId());
                writeD(0);
                writeD((int) (e.getValue() - System.currentTimeMillis()) / 1000);
            }
            writeS(player.getName());
        } else {
            writeH(1);
            writeD(player.getObjectId());
            writeH(1);
            writeD(cooldownId);
            writeD(0);
            writeD((int) (player.getPortalCooldownList().getPortalCooldown(worldId) - System.currentTimeMillis()) / 1000);
            writeS(player.getName());
        }
    }
}
