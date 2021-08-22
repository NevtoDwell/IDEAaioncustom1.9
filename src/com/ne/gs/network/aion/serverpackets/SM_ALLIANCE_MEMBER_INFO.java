/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.List;

import com.ne.gs.configs.main.GSConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.stats.container.PlayerLifeStats;
import com.ne.gs.model.team2.alliance.PlayerAllianceMember;
import com.ne.gs.model.team2.common.legacy.PlayerAllianceEvent;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.world.WorldPosition;

/**
 * @author Sarynth (Thx Rhys2002 for Packets)
 */
public class SM_ALLIANCE_MEMBER_INFO extends AionServerPacket {

    private final Player player;
    private PlayerAllianceEvent event;
    private final int allianceId;
    private final int objectId;

    public SM_ALLIANCE_MEMBER_INFO(PlayerAllianceMember member, PlayerAllianceEvent event) {
        player = member.getObject();
        this.event = event;
        allianceId = member.getAllianceId();
        objectId = member.getObjectId();
    }

    @Override
    protected void writeImpl(AionConnection con) {
        PlayerCommonData pcd = player.getCommonData();
        WorldPosition wp = pcd.getPosition();

        /**
         * Required so that when member is disconnected, and his playerAllianceGroup slot is changed, he will continue to appear as disconnected to the
         * alliance.
         */
        if (event == PlayerAllianceEvent.ENTER && !player.isOnline()) {
            event = PlayerAllianceEvent.ENTER_OFFLINE;
        }

        writeD(allianceId);
        writeD(objectId);
        if (player.isOnline()) {
            PlayerLifeStats pls = player.getLifeStats();
            writeD(pls.getMaxHp());
            writeD(pls.getCurrentHp());
            writeD(pls.getMaxMp());
            writeD(pls.getCurrentMp());
            writeD(pls.getMaxFp());
            writeD(pls.getCurrentFp());
        } else {
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
        }
        if (GSConfig.SERVER_VERSION.startsWith("3.1")) {
            writeD(0);
        }
        writeD(wp.getMapId());
        writeD(wp.getMapId());
        writeF(wp.getX());
        writeF(wp.getY());
        writeF(wp.getZ());
        writeC(pcd.getPlayerClass().getClassId());
        writeC(pcd.getGender().getGenderId());
        writeC(pcd.getLevel());
        writeC(event.getId());
        writeH(0x00); // channel 0x01?
        writeC(0x0);
        switch (event) {
            case LEAVE:
            case LEAVE_TIMEOUT:
            case BANNED:
            case MOVEMENT:
            case DISCONNECTED:
                break;

            case JOIN:
            case ENTER:
            case ENTER_OFFLINE:
            case UPDATE:
            case RECONNECT:
            case APPOINT_VICE_CAPTAIN: // Unused maybe...
            case DEMOTE_VICE_CAPTAIN:
            case APPOINT_CAPTAIN:
                writeS(pcd.getName());
                writeD(0x00); // unk
                writeD(0x00); // unk
                if (player.isOnline()) {
                    List<Effect> abnormalEffects = player.getEffectController().getAbnormalEffects();
                    writeH(abnormalEffects.size());
                    for (Effect effect : abnormalEffects) {
                        writeD(effect.getEffectorId());
                        writeH(effect.getSkillId());
                        writeC(effect.getSkillLevel());
                        writeC(effect.getTargetSlot());
                        writeD(effect.getRemainingTime());
                    }
                } else {
                    writeH(0);
                }
                break;
            case MEMBER_GROUP_CHANGE:
                writeS(pcd.getName());
                break;
            default:
                break;
        }
    }

}
