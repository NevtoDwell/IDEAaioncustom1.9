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
import com.ne.gs.network.aion.AionClientPacket;

public class CM_RELEASE_OBJECT extends AionClientPacket {

    int targetObjectId;

    @Override
    protected void readImpl() {
        targetObjectId = readD();
    }

    @Override
    protected void runImpl() {
        Player player = getConnection().getActivePlayer();
        if (player == null) {
            return;
        }
        //TODO
        //		if (player.getController().hasTask(TaskId.HOUSE_OBJECT_USE)) {
        //			final VisibleObject object = World.getInstance().findVisibleObject(targetObjectId);
        //			if ((!(object instanceof UseableItemObject)) || (player.getController().hasScheduledTask(TaskId.HOUSE_OBJECT_USE))) {
        //				player.getController().cancelTask(TaskId.HOUSE_OBJECT_USE);
        //				sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_CANCEL_USE);
        //			}
        //		}
    }
}
