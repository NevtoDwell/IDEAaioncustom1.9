/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import java.util.Collection;

import com.ne.gs.configs.main.GSConfig;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.gameobjects.player.PlayerCommonData;
import com.ne.gs.model.stats.container.PlayerLifeStats;
import com.ne.gs.model.team2.common.legacy.GroupEvent;
import com.ne.gs.model.team2.group.PlayerGroup;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;
import com.ne.gs.skillengine.model.Effect;
import com.ne.gs.world.WorldPosition;

/**
 * @author Lyahim, ATracer
 */
public class SM_GROUP_MEMBER_INFO extends AionServerPacket {

    private final int groupId;
    private final Player player;
    private GroupEvent event;

    public SM_GROUP_MEMBER_INFO(PlayerGroup group, Player player, GroupEvent event) {
        groupId = group.getTeamId();
        this.player = player;
        this.event = event;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        PlayerLifeStats pls = player.getLifeStats();
        PlayerCommonData pcd = player.getCommonData();
        WorldPosition wp = pcd.getPosition();

        if (event == GroupEvent.ENTER && !player.isOnline()) {
            event = GroupEvent.ENTER_OFFLINE;
        }

        writeD(groupId);
        writeD(player.getObjectId());
        if (player.isOnline()) {
            writeD(pls.getMaxHp());
            writeD(pls.getCurrentHp());
            writeD(pls.getMaxMp());
            writeD(pls.getCurrentMp());
            writeD(pls.getMaxFp()); // maxflighttime
            writeD(pls.getCurrentFp()); // currentflighttime
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
        writeC(pcd.getPlayerClass().getClassId()); // class id
        writeC(pcd.getGender().getGenderId()); // gender id
        writeC(pcd.getLevel()); // level

        writeC(event.getId()); // something events
        writeH(player.isOnline() ? 1 : 0); // TODO channel?
        writeC(player.isMentor() ? 0x01 : 0x00);

        switch (event) {
            case MOVEMENT:
                break;
            case LEAVE:
                writeH(0x00); // unk
                writeC(0x00); // unk
                break;
            case ENTER_OFFLINE:
            case JOIN:
                writeS(pcd.getName()); // name
                break;
            default:
                writeS(pcd.getName()); // name
                writeD(0x00); // unk
                writeD(0x00); // unk
                Collection<Effect> abnormalEffects = player.getEffectController().getAbnormalEffects();
                writeH(abnormalEffects.size()); // Abnormal effects
                for (Effect effect : abnormalEffects) {
                    writeD(effect.getEffectorId()); // casterid
                    writeH(effect.getSkillId()); // spellid
                    writeC(effect.getSkillLevel()); // spell level
                    writeC(effect.getTargetSlot()); // unk ?
                    writeD(effect.getRemainingTime()); // estimatedtime
                }
                writeD(0x25F7); // unk 9719
                break;
        }
    }
}
