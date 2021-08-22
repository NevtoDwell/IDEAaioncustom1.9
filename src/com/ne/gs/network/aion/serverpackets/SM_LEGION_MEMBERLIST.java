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

import com.ne.commons.utils.collections.Partitioner;
import com.ne.gs.model.gameobjects.player.Player;
import com.ne.gs.model.team.legion.LegionMemberEx;
import com.ne.gs.modules.housing.HouseInfo;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * @author Simple
 */
public class SM_LEGION_MEMBERLIST extends AionServerPacket {

    private static final int OFFLINE = 0x00;
    private static final int ONLINE = 0x01;

    private final List<LegionMemberEx> legionMembers;
    private final boolean isFirst;

    private SM_LEGION_MEMBERLIST(List<LegionMemberEx> legionMembers, boolean isFirst) {
        this.legionMembers = legionMembers;
        this.isFirst = isFirst;
    }

    @Override
    protected void writeImpl(AionConnection con) {
        writeC(isFirst ? 0x01 : 0x00);
        writeH(65536 - legionMembers.size()); // FIXME wtf?? unsigned type??
        for (LegionMemberEx legionMember : legionMembers) {
            writeD(legionMember.getObjectId());
            writeS(legionMember.getName());
            writeC(legionMember.getPlayerClass().getClassId());
            writeD(legionMember.getLevel());
            writeC(legionMember.getRank().getRankId());
            writeD(legionMember.getWorldId());
            writeC(legionMember.isOnline() ? ONLINE : OFFLINE);
            writeS(legionMember.getSelfIntro());
            writeS(legionMember.getNickname());
            writeD(legionMember.getLastOnline());

            HouseInfo info = HouseInfo.of(legionMember.getObjectId());
            writeD(info.getId());
            writeD(info.getHouseId());
            writeD(info.getAccessId());

            writeD(1);
        }
    }

    public static void sendTo(final Player player, Iterable<LegionMemberEx> members) {
        Partitioner.of(members, 30).foreach(new Partitioner.Func2<LegionMemberEx>() {
            @Override
            public boolean apply(List<LegionMemberEx> list, boolean first, boolean last) {
                player.sendPck(new SM_LEGION_MEMBERLIST(list, first));
                return true;
            }
        });
    }
}
