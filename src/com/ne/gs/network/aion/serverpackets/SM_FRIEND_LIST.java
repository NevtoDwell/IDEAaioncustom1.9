/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.network.aion.serverpackets;

import com.ne.gs.model.gameobjects.player.Friend;
import com.ne.gs.model.gameobjects.player.FriendList;
import com.ne.gs.modules.housing.HouseInfo;
import com.ne.gs.network.aion.AionConnection;
import com.ne.gs.network.aion.AionServerPacket;

/**
 * Sends a friend list to the client
 *
 * @author Ben
 */
public class SM_FRIEND_LIST extends AionServerPacket {

    @Override
    protected void writeImpl(AionConnection con) {
        FriendList list = con.getActivePlayer().getFriendList();

        writeH((0 - list.getSize()));
        writeC(0); // Unk

        for (Friend friend : list) {
            writeD(friend.getOid());
            writeS(friend.getName());
            writeD(friend.getLevel());
            writeD(friend.getPlayerClass().getClassId());
            writeC(friend.isOnline() ? 1 : 0);
            writeD(friend.getMapId());
            writeD(friend.getLastOnlineTime()); // Date friend was last online as a Unix timestamp.
            writeS(friend.getNote()); // Friend note
            writeC(friend.getStatus().getId());

            HouseInfo info = HouseInfo.of(friend.getOid());

            writeD(info.getId());  //https://free-redmine.saas-secure.com/issues/11756
            writeD(info.getHouseId());
            writeC(info.getAccessId());
        }
    }
}
